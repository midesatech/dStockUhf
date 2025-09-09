package infrastructure.fx.controller.dashboard;

import domain.model.LocationPresence;
import domain.model.Occupant;
import domain.usecase.DashboardUseCase;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Dashboard pastel de ocupación por ubicación.
 *  Approach A: calcula presencia directo desde detecciones_tags (sin escrituras).
 */
public class DashboardController {

    @FXML private Region root;
    @FXML private FlowPane tiles;
    @FXML private Label subtitle;
    @FXML private ScrollPane scroller;

    @FXML private TableView<OccupantRow> detailsTable;
    @FXML private TableColumn<OccupantRow, String> colTipo;
    @FXML private TableColumn<OccupantRow, String> colEpc;
    @FXML private TableColumn<OccupantRow, String> colNombre;
    @FXML private TableColumn<OccupantRow, String> colUltimo;
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblTotalEquipment;

    // NUEVO: totales de presencia actual
    @FXML private Label lblPresentEmployees;
    @FXML private Label lblPresentEquipment;

    // KPIs
    @FXML private SplitPane splitMain;
    @FXML private BorderPane kpiPanel;     // contenedor fijo con header
    @FXML private AnchorPane kpiContent;   // contenido colapsable (KPIs)
    @FXML private Button btnToggleKpis;

    // Ubicaciones
    @FXML private SplitPane splitContent;  // divisor entre Ubicaciones y Detalle
    @FXML private BorderPane tilesPanel;   // contenedor fijo (header + contenido)
    @FXML private ScrollPane tilesContent; // contenido colapsable (widgets)
    @FXML private Button btnToggleTiles;

    @FXML private VBox kpiPresentEmpCard;
    @FXML private VBox kpiPresentEqpCard;


    private final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<VBox> cardPool = new ArrayList<>();
    private final Timer timer = new Timer("dashboard-refresh", true);
    private int timeoutSeconds = 86400; // configurable
    private javafx.scene.Node selectedTile;
    private TimerTask refreshTask;
    // Use case hexagonal
    private final DashboardUseCase dashboard;
    // Guardamos la última posición del divisor para restaurarla al expandir
    private Double lastDividerPos = null;
    private Double lastTilesDividerPos = null;
    private int lastTotalEmployees = 0;
    private int lastTotalEquipment = 0;
    private int lastPresentEmployees = 0;
    private int lastPresentEquipment = 0;

    // Evitar re-entradas y bucles del combo
    private volatile boolean cmbPopulating = false;
    private volatile boolean fetchInProgress = false;

    // Cache del último dataset completo (para re-filtrar sin ir a DB)
    private List<LocationPresence> lastAllData = List.of();


    // Combo de ubicaciones
    @FXML private ComboBox<LocationOption> cmbUbicaciones;
    private Long selectedLocationId = null; // null = "Todos"

    // Indica si el filtrado de tiles está ACTIVADO por el ComboBox
    private boolean filterFromCombo = false;

    public DashboardController(DashboardUseCase dashboard) {
        this.dashboard = dashboard;
    }

    @FXML
    public void initialize() {
        String env = System.getenv("PRESENCE_TIMEOUT_SECONDS");
        if (env != null && !env.isEmpty()) {
            try { timeoutSeconds = Integer.parseInt(env); } catch (NumberFormatException ignored) {}
        }
        subtitle.setText("Últimas lecturas en " + timeoutSeconds + "s (detecciones_tags)");

        setupDetailsTable();
        updateTotals();
        refreshNowAsync();

        // Normaliza clases por si quedaron definidas con punto desde el FXML antiguo
        normalizeStyleClasses(kpiPresentEmpCard);
        normalizeStyleClasses(kpiPresentEqpCard);
        // Asegura que tienen base 'kpi-card'
        if (kpiPresentEmpCard != null && !kpiPresentEmpCard.getStyleClass().contains("kpi-card"))
            kpiPresentEmpCard.getStyleClass().add(0, "kpi-card");
        if (kpiPresentEqpCard != null && !kpiPresentEqpCard.getStyleClass().contains("kpi-card"))
            kpiPresentEqpCard.getStyleClass().add(0, "kpi-card");

        // Aplica estados segun valores ya calculados
        updateKpiStates();

        // Opcional: posición inicial del divisor (30% KPIs / 70% contenido)
        if (splitMain != null && !splitMain.getDividers().isEmpty()) {
            splitMain.getDividers().get(0).setPosition(0.19);
        }

        // Posición inicial del divisor entre Ubicaciones y Detalle
        if (splitContent != null && !splitContent.getDividers().isEmpty()) {
            splitContent.getDividers().get(0).setPosition(0.60);
        }

        // Arrancar/parar según visibilidad del nodo (útil cuando cambias el centro de un BorderPane)
        root.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible) startPolling(); else stopPolling();
        });

        // Arrancar/parar según se muestre/oculte la ventana (útil al cambiar de escena o minimizar)
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            // Si nos quitan la escena, paramos
            if (newScene == null) {
                stopPolling();
                return;
            }
            newScene.windowProperty().addListener((o2, oldW, newW) -> {
                if (oldW != null) oldW.showingProperty().removeListener(windowShowingListener);
                if (newW != null) newW.showingProperty().addListener(windowShowingListener);
            });
        });

        if (cmbUbicaciones != null) {
            cmbUbicaciones.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (cmbPopulating) return; // evitar bucle al repoblar

                if (newVal == null || newVal.id == null) {
                    // "Todos" ⇒ NO filtrar tiles
                    filterFromCombo = false;
                    selectedLocationId = null;
                    applyData(lastAllData);       // re-render con TODO
                    loadDetailsAsync(null, null); // limpiar detalle
                } else {
                    // Ubicación específica ⇒ filtrar tiles
                    filterFromCombo = true;
                    selectedLocationId = newVal.id;
                    applyData(lastAllData);            // re-render filtrado
                    loadDetailsAsync(newVal.id, newVal.name); // carga detalle de esa ubicación
                }

                if (scroller != null) scroller.setVvalue(0.0);
            });
        }

        // Si ya está visible y la ventana está mostrando, arrancamos
        Platform.runLater(() -> {
            if (root.isVisible()
                    && root.getScene() != null
                    && root.getScene().getWindow() != null
                    && root.getScene().getWindow().isShowing()) {
                startPolling();
            }
        });
    }

    private final ChangeListener<Boolean> windowShowingListener = (obs, wasShowing, isShowing) -> {
        if (isShowing) startPolling(); else stopPolling();
    };

    private void startPolling() {
        if (refreshTask != null) return; // ya está corriendo
        refreshTask = new TimerTask() {
            @Override public void run() {
                refreshNowAsync();  // 👈 NO Platform.runLater: ya hacemos UI en onSucceeded
            }
        };
        timer.scheduleAtFixedRate(refreshTask, 2000, 5000); // 2s primer tick, luego cada 5s
    }

    // Consulta en background y actualiza UI en onSucceeded
    private void refreshNowAsync() {
        if (fetchInProgress) return; // 👈 evitar concurrencias (tick anterior no ha terminado)
        fetchInProgress = true;

        final LocalDateTime since = LocalDateTime.now().minusSeconds(timeoutSeconds);

        javafx.concurrent.Task<List<LocationPresence>> task = new javafx.concurrent.Task<>() {
            @Override protected List<LocationPresence> call() {
                // ⚠️ Aquí estamos en hilo en background (NO UI)
                return dashboard.getPresenceSince(since);
            }
        };

        task.setOnSucceeded(evt -> {
            try {
                List<LocationPresence> allData = task.getValue();
                applyData(allData); // 👈 aquí sí tocamos UI
            } finally {
                fetchInProgress = false;
            }
        });

        task.setOnFailed(evt -> {
            fetchInProgress = false;
            // (opcional) registra el error: task.getException()
        });

        Thread t = new Thread(task, "dashboard-refresh-task");
        t.setDaemon(true);
        t.start();
    }

    /** Aplica datos a la UI (tarjetas, presentes, KPIs) y repuebla combo sin bucles */
    private void applyData(List<LocationPresence> allData) {
        if (allData == null) allData = List.of();
        lastAllData = allData; // cache

        // 1) Repoblar combo (sin disparar listener)
        populateLocationsCombo(allData);

        // 2) Filtrar SOLO las tarjetas según el combo
        // 2) Filtrar SOLO si el filtro proviene del ComboBox
        List<LocationPresence> data = (!filterFromCombo || selectedLocationId == null)
                ? allData
                : allData.stream().filter(lp -> lp.getLocationId() == selectedLocationId).toList();

        // 3) Render de tarjetas
        ensureCards(data.size());
        for (int i = 0; i < data.size(); i++) {
            LocationPresence lp = data.get(i);
            VBox card = (VBox) tiles.getChildren().get(i);
            updateCard(card, lp, i);
        }
        while (tiles.getChildren().size() > data.size()) {
            int last = tiles.getChildren().size() - 1;
            tiles.getChildren().remove(last);
            cardPool.remove(last);
        }

        // 4) Presentes AHORA (KPIs) SIEMPRE con ALLDATA (no filtrado)
        int presentEmpAll = allData.stream().mapToInt(LocationPresence::getEmployees).sum();
        int presentEqpAll = allData.stream().mapToInt(LocationPresence::getEquipment).sum();
        lastPresentEmployees = presentEmpAll;
        lastPresentEquipment = presentEqpAll;
        if (lblPresentEmployees != null) lblPresentEmployees.setText(String.valueOf(presentEmpAll));
        if (lblPresentEquipment != null) lblPresentEquipment.setText(String.valueOf(presentEqpAll));

        // 5) Totales globales y estados de KPI
        updateTotals();
        updateKpiStates();
    }


    private void stopPolling() {
        if (refreshTask != null) {
            refreshTask.cancel();
            refreshTask = null;
        }
    }

    /** Llama esto explícitamente si tu “router” destruye o reemplaza el controller */
    public void dispose() {
        stopPolling();
        timer.purge();
    }

    private void refreshNow() {
        LocalDateTime since = LocalDateTime.now().minusSeconds(timeoutSeconds);

        // 1) Traer TODO para KPIs y para construir la lista de ubicaciones
        List<LocationPresence> allData = dashboard.getPresenceSince(since);

        // 2) Poblamos el combo (conserva selección si existe)
        populateLocationsCombo(allData);

        // 3) Aplicar filtro SOLO para las tarjetas (si el usuario eligió ubicación)
        List<LocationPresence> data = allData;
        if (selectedLocationId != null) {
            data = allData.stream()
                    .filter(lp -> lp.getLocationId() == selectedLocationId)
                    .toList();
        }

        // 4) Tarjetas (grid) con DATA FILTRADA
        ensureCards(data.size());
        for (int i = 0; i < data.size(); i++) {
            LocationPresence lp = data.get(i);
            VBox card = (VBox) tiles.getChildren().get(i);
            updateCard(card, lp, i);
        }
        while (tiles.getChildren().size() > data.size()) {
            int last = tiles.getChildren().size() - 1;
            tiles.getChildren().remove(last);
            cardPool.remove(last);
        }

        // 5) KPIs SIEMPRE con ALLDATA (NO FILTRADO)
        int presentEmpAll = allData.stream().mapToInt(LocationPresence::getEmployees).sum();
        int presentEqpAll = allData.stream().mapToInt(LocationPresence::getEquipment).sum();
        lastPresentEmployees = presentEmpAll;
        lastPresentEquipment = presentEqpAll;
        if (lblPresentEmployees != null) lblPresentEmployees.setText(String.valueOf(presentEmpAll));
        if (lblPresentEquipment != null) lblPresentEquipment.setText(String.valueOf(presentEqpAll));

        updateTotals();     // totales globales (DB completa)
        updateKpiStates();  // warn/ok (comparando presentes globales vs totales globales)
    }

    private void ensureCards(int n) {
        while (tiles.getChildren().size() < n) {
            VBox card = makeCard();
            tiles.getChildren().add(card);
            cardPool.add(card);
        }
    }

    private VBox makeCard() {
        VBox box = new VBox();
        box.getStyleClass().addAll("location-card", "bg-0");
        box.setPickOnBounds(true);
        box.setStyle("-fx-cursor: hand;");
        Label title = new Label("Ubicación");
        title.getStyleClass().add("card-title");
        Label emp = new Label("Personas: 0");
        emp.getStyleClass().add("card-metric");
        Label eq  = new Label("Equipos: 0");
        eq.getStyleClass().add("card-metric");
        box.getChildren().addAll(title, emp, eq);
        return box;
    }

    private void updateCard(VBox card, LocationPresence lp, int index) {
        // pastel de fondo
        String cls = "bg-" + (index % 6);
        card.getStyleClass().removeIf(s -> s.startsWith("bg-"));
        card.getStyleClass().add(cls);

        // etiquetas
        ((Label) card.getChildren().get(0)).setText(lp.getLocationName());
        ((Label) card.getChildren().get(1)).setText("Personas: " + lp.getEmployees());
        ((Label) card.getChildren().get(2)).setText("Equipos: " + lp.getEquipment());

        // click handler para cargar detalle
        attachClickHandler(card, lp.getLocationId(), lp.getLocationName());
    }

    private void attachClickHandler(javafx.scene.Node tileNode, long ubicacionId, String ubicacionNombre) {
        tileNode.setOnMouseClicked(e -> onUbicacionSelected(tileNode, ubicacionId, ubicacionNombre));
    }

    private void onUbicacionSelected(javafx.scene.Node tileNode, long ubicacionId, String ubicacionNombre) {
        // NO tocar ComboBox ni selectedLocationId ni filterFromCombo

        // Dispara un refresh inmediato para alinear KPIs/tarjetas si es posible
        if (!fetchInProgress) {
            refreshNowAsync();
        }

        // Cargar detalle (ya filtrado por ventana)
        loadDetailsAsync(ubicacionId, ubicacionNombre);

        // Quitar selección previa visual y animar tarjeta
        if (selectedTile != null) {
            selectedTile.getStyleClass().remove("selected");
            selectedTile.setScaleX(1.0);
            selectedTile.setScaleY(1.0);
        }
        tileNode.getStyleClass().add("selected");
        ScaleTransition st = new ScaleTransition(Duration.millis(120), tileNode);
        st.setToX(1.03);
        st.setToY(1.03);
        st.play();
        selectedTile = tileNode;
    }


    private void setupDetailsTable() {
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colEpc.setCellValueFactory(new PropertyValueFactory<>("epc"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUltimo.setCellValueFactory(new PropertyValueFactory<>("ultimo"));

        // Tipo legible
        colTipo.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) { setText(null); return; }
                String t = tipo.toUpperCase();
                setText(t.contains("EMP") ? "Empleado" : "Equipo");
            }
        });

        // Anchos proporcionales (opcional)
        colTipo.prefWidthProperty().bind(detailsTable.widthProperty().multiply(0.15));
        colEpc.prefWidthProperty().bind(detailsTable.widthProperty().multiply(0.30));
        colNombre.prefWidthProperty().bind(detailsTable.widthProperty().multiply(0.40));
        colUltimo.prefWidthProperty().bind(detailsTable.widthProperty().multiply(0.15));

        // Colores pastel por tipo
        detailsTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(OccupantRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-employee", "row-equipment");
                if (empty || item == null) return;
                String t = item.getTipo() == null ? "" : item.getTipo().toUpperCase();
                if (t.contains("EMP")) getStyleClass().add("row-employee");
                else getStyleClass().add("row-equipment");
            }
        });
    }

    private void updateTotals() {
        try {
            int te = dashboard.totalEmployees();
            int tq = dashboard.totalEquipment();
            lastTotalEmployees = te;
            lastTotalEquipment = tq;

            if (lblTotalEmployees != null) lblTotalEmployees.setText(String.valueOf(te));
            if (lblTotalEquipment != null) lblTotalEquipment.setText(String.valueOf(tq));
        } catch (Exception ignored) { }
    }

    private void updateKpiStates() {
        setKpiState(kpiPresentEmpCard, lastPresentEmployees, lastTotalEmployees);
        setKpiState(kpiPresentEqpCard, lastPresentEquipment, lastTotalEquipment);
    }

    private void setKpiState(VBox card, int present, int total) {
        if (card == null) return;

        // Normaliza por si acaso (si alguien reinyecta FXML con el punto)
        normalizeStyleClasses(card);

        // Asegura base 'kpi-card'
        if (!card.getStyleClass().contains("kpi-card")) {
            card.getStyleClass().add(0, "kpi-card");
        }
        // Limpia estados previos (y la 'accent' para que no compita)
        card.getStyleClass().removeAll("warn", "ok", "accent", "kpi-card.accent");

        // Sólo warning cuando difiere; si prefieres sólo cuando present < total, cambia por (present < total)
        if (present != total) card.getStyleClass().add("warn");
        else card.getStyleClass().add("ok");

        // (opcional) Forzar recomputo CSS de ese nodo
        // card.applyCss();
    }

    private void normalizeStyleClasses(javafx.scene.Node node) {
        if (node == null) return;
        List<String> add = new ArrayList<>();
        List<String> remove = new ArrayList<>();
        for (String c : node.getStyleClass()) {
            if (c.contains(".")) {
                String[] parts = c.split("\\.");
                for (String p : parts) if (!p.isBlank()) add.add(p);
                remove.add(c);
            }
        }
        node.getStyleClass().removeAll(remove);
        node.getStyleClass().addAll(add);
    }


    @FXML
    private void toggleKpis() {
        if (kpiContent == null || splitMain == null || splitMain.getDividers().isEmpty()) return;
        SplitPane.Divider divider = splitMain.getDividers().get(0);

        boolean visible = kpiContent.isVisible();
        if (visible) {
            // Guardar altura actual y colapsar
            lastDividerPos = divider.getPosition();
            kpiContent.setVisible(false);
            kpiContent.setManaged(false);
            divider.setPosition(0.0); // da todo el espacio al panel inferior
            if (btnToggleKpis != null) btnToggleKpis.setText("Mostrar KPIs");
        } else {
            // Mostrar y restaurar altura
            kpiContent.setVisible(true);
            kpiContent.setManaged(true);
            divider.setPosition(lastDividerPos != null ? lastDividerPos : 0.35);
            if (btnToggleKpis != null) btnToggleKpis.setText("Ocultar KPIs");
        }
    }

    @FXML
    private void toggleTiles() {
        if (tilesContent == null || splitContent == null || splitContent.getDividers().isEmpty()) return;
        SplitPane.Divider divider = splitContent.getDividers().get(0);

        boolean visible = tilesContent.isVisible();
        if (visible) {
            lastTilesDividerPos = divider.getPosition();
            tilesContent.setVisible(false);
            tilesContent.setManaged(false);
            divider.setPosition(0.0); // todo el espacio para Detalle
            if (btnToggleTiles != null) btnToggleTiles.setText("Mostrar Ubicaciones");
        } else {
            tilesContent.setVisible(true);
            tilesContent.setManaged(true);
            divider.setPosition(lastTilesDividerPos != null ? lastTilesDividerPos : 0.60);
            if (btnToggleTiles != null) btnToggleTiles.setText("Ocultar Ubicaciones");
        }
    }

    private void populateLocationsCombo(List<LocationPresence> allData) {
        if (cmbUbicaciones == null) return;

        cmbPopulating = true;        // 👈 bloquear listener
        try {
            Long previous = selectedLocationId; // conservar selección actual

            List<LocationOption> items = new ArrayList<>();
            items.add(new LocationOption(null, "Todos"));

            java.util.LinkedHashMap<Long, String> map = new java.util.LinkedHashMap<>();
            for (LocationPresence lp : allData) {
                map.put(lp.getLocationId(), lp.getLocationName());
            }
            map.entrySet().stream()
                    .sorted((a, b) -> a.getValue().compareToIgnoreCase(b.getValue()))
                    .forEach(e -> items.add(new LocationOption(e.getKey(), e.getValue())));

            cmbUbicaciones.getItems().setAll(items);

            LocationOption sel = items.stream()
                    .filter(lo -> (previous == null && lo.id == null) ||
                            (previous != null && lo.id != null && lo.id.equals(previous)))
                    .findFirst()
                    .orElse(items.get(0));

            cmbUbicaciones.getSelectionModel().select(sel);
            selectedLocationId = sel.id;
        } finally {
            cmbPopulating = false;   // 👈 reactivar listener
        }
    }

    private void loadDetailsAsync(Long ubicacionId, String ubicacionNombre) {
        // Si es "Todos", limpiamos detalle y subtítulo
        if (ubicacionId == null) {
            subtitle.setText("Todas las ubicaciones");
            detailsTable.getItems().clear();
            if (selectedTile != null) {
                selectedTile.getStyleClass().remove("selected");
                selectedTile = null;
            }
            return;
        }

        subtitle.setText("Ubicación: " + ubicacionNombre);

        // Usa el MISMO since que presence
        final LocalDateTime since = LocalDateTime.now().minusSeconds(timeoutSeconds);

        javafx.concurrent.Task<List<Occupant>> task = new javafx.concurrent.Task<>() {
            @Override protected List<Occupant> call() {
                // Trae todos los ocupantes de la ubicación…
                List<Occupant> all = dashboard.getOccupantsByUbicacion(ubicacionId);
                // …y FILTRA por la misma ventana temporal para alinear con KPIs/tarjetas
                List<Occupant> filtered = new ArrayList<>();
                for (Occupant o : all) {
                    if (o.getLastSeen() != null && !o.getLastSeen().isBefore(since)) {
                        filtered.add(o);
                    }
                }
                return filtered;
            }
        };

        task.setOnSucceeded(evt -> {
            List<Occupant> oc = task.getValue();
            List<OccupantRow> rows = new ArrayList<>();
            for (Occupant o : oc) {
                String last = (o.getLastSeen() == null) ? "" : o.getLastSeen().format(TS_FMT);
                rows.add(new OccupantRow(o.getTipo(), o.getEpc(), o.getNombre(), last));
            }
            detailsTable.setItems(FXCollections.observableArrayList(rows));
        });

        task.setOnFailed(evt -> {
            detailsTable.getItems().clear();
            // opcional: log task.getException()
        });

        Thread t = new Thread(task, "load-details");
        t.setDaemon(true);
        t.start();
    }

    // Auxiliar de combo
    private static class LocationOption {
        final Long id;      // null = Todos
        final String name;
        LocationOption(Long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

}
