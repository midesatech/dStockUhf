package infrastructure.fx.controller.dashboard;

import domain.model.LocationPresence;
import domain.model.Occupant;
import domain.usecase.DashboardUseCase;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Dashboard de ocupación por ubicación.
 * - JavaFX ScheduledService para polling (sin bloquear UI ni solapar ejecuciones)
 * - KPIs siempre calculados con ALL DATA (no filtrado)
 * - Filtro de tarjetas sólo cuando el usuario cambia el ComboBox
 * - Click en tarjeta NO altera el filtro del combo (sólo carga el detalle)
 */
public class DashboardController {

    // ==== Inyectados por FXML ====
    @FXML private Region root;

    // Header
    @FXML private Label subtitle;

    // Tiles (ubicaciones)
    @FXML private FlowPane tiles;
    @FXML private ScrollPane scroller;

    // Tabla de detalle
    @FXML private TableView<OccupantRow> detailsTable;
    @FXML private TableColumn<OccupantRow, String> colTipo;
    @FXML private TableColumn<OccupantRow, String> colEpc;
    @FXML private TableColumn<OccupantRow, String> colNombre;
    @FXML private TableColumn<OccupantRow, String> colUltimo;

    // KPIs
    @FXML private Label lblTotalEmployees;
    @FXML private Label lblTotalEquipment;
    @FXML private Label lblPresentEmployees;
    @FXML private Label lblPresentEquipment;

    // Paneles/colapsables
    @FXML private SplitPane splitMain;
    @FXML private AnchorPane kpiContent;
    @FXML private Button btnToggleKpis;

    @FXML private SplitPane splitContent;
    @FXML private ScrollPane tilesContent;
    @FXML private Button btnToggleTiles;

    // KPI cards para estados visuales (warn/ok)
    @FXML private VBox kpiPresentEmpCard;
    @FXML private VBox kpiPresentEqpCard;

    // Combo de ubicaciones
    @FXML private ComboBox<LocationOption> cmbUbicaciones;

    // ==== Configuración ====
    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final double MAIN_SPLIT_DEFAULT = 0.19;   // altura inicial de KPIs
    private static final double CONTENT_SPLIT_DEFAULT = 0.60;// tiles vs detalle
    private static final int DEFAULT_TIMEOUT_SECONDS = 86400;
    private static final int POLL_PERIOD_SECONDS = 5;
    private static final int POLL_INITIAL_DELAY_SECONDS = 2;

    // ==== Estado ====
    private final DashboardUseCase dashboard;

    private int timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

    private ScheduledService<List<LocationPresence>> poller;

    private final List<VBox> cardPool = new ArrayList<>();
    private javafx.scene.Node selectedTile;

    // Cache y conteos
    private List<LocationPresence> lastAllData = List.of();
    private int lastTotalEmployees = 0;
    private int lastTotalEquipment = 0;
    private int lastPresentEmployees = 0;
    private int lastPresentEquipment = 0;

    // Filtro por ComboBox
    private Long selectedLocationId = null; // null = "Todos"
    private boolean filterFromCombo = false;
    private final AtomicBoolean cmbPopulating = new AtomicBoolean(false);

    // Recordatorio de divisores al colapsar
    private Double lastDividerPos = null;
    private Double lastTilesDividerPos = null;

    public DashboardController(DashboardUseCase dashboard) {
        this.dashboard = dashboard;
    }

    // ==== Ciclo de vida ====
    @FXML
    public void initialize() {
        // Timeout por env var
        String env = System.getenv("PRESENCE_TIMEOUT_SECONDS");
        if (env != null && !env.isEmpty()) {
            try { timeoutSeconds = Integer.parseInt(env); } catch (NumberFormatException ignored) {}
        }
        subtitle.setText("Últimas lecturas en " + timeoutSeconds + "s (detecciones_tags)");

        setupDetailsTable();
        normalizeStyleClasses(kpiPresentEmpCard);
        normalizeStyleClasses(kpiPresentEqpCard);
        ensureKpiBaseClass(kpiPresentEmpCard);
        ensureKpiBaseClass(kpiPresentEqpCard);

        // Posición inicial de splitters
        if (splitMain != null && !splitMain.getDividers().isEmpty())
            splitMain.getDividers().get(0).setPosition(MAIN_SPLIT_DEFAULT);
        if (splitContent != null && !splitContent.getDividers().isEmpty())
            splitContent.getDividers().get(0).setPosition(CONTENT_SPLIT_DEFAULT);

        // Polling según visibilidad/ventana
        root.visibleProperty().addListener((obs, wasVisible, isVisible) -> {
            if (isVisible) startPolling(); else stopPolling();
        });
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) { stopPolling(); return; }
            newScene.windowProperty().addListener((o2, oldW, newW) -> {
                if (oldW != null) oldW.showingProperty().removeListener(windowShowingListener);
                if (newW != null) newW.showingProperty().addListener(windowShowingListener);
            });
        });

        // Combo cambios
        if (cmbUbicaciones != null) {
            cmbUbicaciones.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (cmbPopulating.get()) return;
                if (newVal == null || newVal.id == null) {
                    // "Todos" ⇒ no filtrar tiles
                    filterFromCombo = false;
                    selectedLocationId = null;
                    renderFromCache();
                    loadDetailsAsync(null, null); // limpiar detalle
                } else {
                    // Filtrar tiles por ubicación
                    filterFromCombo = true;
                    selectedLocationId = newVal.id;
                    renderFromCache();
                    loadDetailsAsync(newVal.id, newVal.name);
                }
                if (scroller != null) scroller.setVvalue(0.0);
            });
        }

        // Arranque si ya está visible
        Platform.runLater(() -> {
            if (root.isVisible()
                    && root.getScene() != null
                    && root.getScene().getWindow() != null
                    && root.getScene().getWindow().isShowing()) {
                startPolling();
            }
        });

        // Pinta totales una vez (si hay) mientras llega primer tick
        updateTotals();
        updateKpiStates();
    }

    private final ChangeListener<Boolean> windowShowingListener = (obs, wasShowing, isShowing) -> {
        if (isShowing) startPolling(); else stopPolling();
    };

    // ==== Polling ====
    private void startPolling() {
        if (poller != null && poller.isRunning()) return;
        poller = new ScheduledService<>() {
            @Override protected Task<List<LocationPresence>> createTask() {
                final LocalDateTime since = buildSince();
                return new Task<>() {
                    @Override protected List<LocationPresence> call() {
                        return dashboard.getPresenceSince(since);
                    }
                };
            }
        };
        poller.setPeriod(Duration.seconds(POLL_PERIOD_SECONDS));
        poller.setDelay(Duration.seconds(POLL_INITIAL_DELAY_SECONDS));
        poller.setRestartOnFailure(true);
        poller.setOnSucceeded(e -> {
            List<LocationPresence> allData = poller.getValue();
            if (allData == null) allData = List.of();
            applyAllData(allData);
        });
        poller.setOnFailed(e -> {
            // (opcional) loggear poller.getException()
        });
        poller.start();
        poller.restart();
    }

    private void stopPolling() {
        if (poller != null) {
            poller.cancel();
            poller = null;
        }
    }

    /** Si el “router” destruye/reemplaza el controller */
    public void dispose() {
        stopPolling();
    }

    // ==== Render principal ====
    private void applyAllData(List<LocationPresence> allData) {
        this.lastAllData = allData;

        // 1) combo: repoblar sólo si cambió el set de ubicaciones
        repopulateLocationsComboIfNeeded(allData);

        // 2) tiles segun filtro actual
        renderFromCache();

        // 3) KPIs SIEMPRE con ALLDATA (no filtrado)
        updateKpisFrom(allData);

        // 4) estilos de KPI (ok/warn)
        updateKpiStates();
    }

    private void renderFromCache() {
        final List<LocationPresence> base = this.lastAllData;
        final List<LocationPresence> data =
                (!filterFromCombo || selectedLocationId == null)
                        ? base
                        : base.stream().filter(lp -> lp.getLocationId() == selectedLocationId).toList();

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
    }

    private void updateKpisFrom(List<LocationPresence> allData) {
        int presentEmpAll = allData.stream().mapToInt(LocationPresence::getEmployees).sum();
        int presentEqpAll = allData.stream().mapToInt(LocationPresence::getEquipment).sum();

        lastPresentEmployees = presentEmpAll;
        lastPresentEquipment = presentEqpAll;

        if (lblPresentEmployees != null) lblPresentEmployees.setText(String.valueOf(presentEmpAll));
        if (lblPresentEquipment != null) lblPresentEquipment.setText(String.valueOf(presentEqpAll));

        updateTotals();
    }

    // ==== Tiles ====
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
        // background pastel
        String cls = "bg-" + (index % 6);
        card.getStyleClass().removeIf(s -> s.startsWith("bg-"));
        card.getStyleClass().add(cls);

        // etiquetas
        ((Label) card.getChildren().get(0)).setText(lp.getLocationName());
        ((Label) card.getChildren().get(1)).setText("Personas: " + lp.getEmployees());
        ((Label) card.getChildren().get(2)).setText("Equipos: " + lp.getEquipment());

        // click: solo detalle y selección visual (NO toca combo ni filtro)
        attachClickHandler(card, lp.getLocationId(), lp.getLocationName());
    }

    private void attachClickHandler(javafx.scene.Node tileNode, long ubicacionId, String ubicacionNombre) {
        tileNode.setOnMouseClicked(e -> onTileClicked(tileNode, ubicacionId, ubicacionNombre));
    }

    private void onTileClicked(javafx.scene.Node tileNode, long ubicacionId, String ubicacionNombre) {
        // Refresh inmediato para KPIs/tiles (no obligatorio, mejora percepción)
        if (poller == null || !poller.isRunning()) { startPolling(); }

        loadDetailsAsync(ubicacionId, ubicacionNombre);

        // selección visual
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

    // ==== Tabla de detalle ====
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

        // Colores por tipo
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

    private void loadDetailsAsync(Long ubicacionId, String ubicacionNombre) {
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

        final LocalDateTime since = buildSince();

        Task<List<Occupant>> task = new Task<>() {
            @Override protected List<Occupant> call() {
                List<Occupant> all = dashboard.getOccupantsByUbicacion(ubicacionId);
                if (all == null) return List.of();
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
            // (opcional) log task.getException()
        });

        Thread t = new Thread(task, "load-details");
        t.setDaemon(true);
        t.start();
    }

    // ==== KPIs ====
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
        normalizeStyleClasses(card);
        ensureKpiBaseClass(card);
        card.getStyleClass().removeAll("warn", "ok", "accent", "kpi-card.accent");
        if (present != total) card.getStyleClass().add("warn");
        else card.getStyleClass().add("ok");
    }

    private void ensureKpiBaseClass(VBox card) {
        if (card != null && !card.getStyleClass().contains("kpi-card")) {
            card.getStyleClass().add(0, "kpi-card");
        }
    }

    // ==== Colapsables ====
    @FXML
    private void toggleKpis() {
        if (kpiContent == null || splitMain == null || splitMain.getDividers().isEmpty()) return;
        SplitPane.Divider divider = splitMain.getDividers().get(0);
        boolean visible = kpiContent.isVisible();
        if (visible) {
            lastDividerPos = divider.getPosition();
            kpiContent.setVisible(false);
            kpiContent.setManaged(false);
            divider.setPosition(0.0);
            if (btnToggleKpis != null) btnToggleKpis.setText("Mostrar KPIs");
        } else {
            kpiContent.setVisible(true);
            kpiContent.setManaged(true);
            divider.setPosition(lastDividerPos != null ? lastDividerPos : MAIN_SPLIT_DEFAULT);
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
            divider.setPosition(0.0);
            if (btnToggleTiles != null) btnToggleTiles.setText("Mostrar Ubicaciones");
        } else {
            tilesContent.setVisible(true);
            tilesContent.setManaged(true);
            divider.setPosition(lastTilesDividerPos != null ? lastTilesDividerPos : CONTENT_SPLIT_DEFAULT);
            if (btnToggleTiles != null) btnToggleTiles.setText("Ocultar Ubicaciones");
        }
    }

    // ==== Combo helpers ====
    private void repopulateLocationsComboIfNeeded(List<LocationPresence> allData) {
        if (cmbUbicaciones == null) return;

        // fingerprint (IDs + nombres ordenados) para no repoblar si no cambió
        String fp = allData.stream()
                .map(lp -> lp.getLocationId() + ":" + (lp.getLocationName() == null ? "" : lp.getLocationName()))
                .distinct()
                .sorted()
                .collect(Collectors.joining("|"));

        Object currentTag = cmbUbicaciones.getProperties().get("fp");
        if (Objects.equals(fp, currentTag) && !cmbUbicaciones.getItems().isEmpty()) {
            return; // sin cambios
        }

        cmbPopulating.set(true);
        try {
            Long previous = selectedLocationId;

            List<LocationOption> items = new ArrayList<>();
            items.add(new LocationOption(null, "Todos"));

            LinkedHashMap<Long, String> map = new LinkedHashMap<>();
            for (LocationPresence lp : allData) map.put(lp.getLocationId(), lp.getLocationName());
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
            // OJO: no cambiamos filterFromCombo aquí; sólo re-seleccionamos visualmente
            selectedLocationId = sel.id;

            // guarda fingerprint actual
            cmbUbicaciones.getProperties().put("fp", fp);
        } finally {
            cmbPopulating.set(false);
        }
    }

    private static class LocationOption {
        final Long id;      // null = Todos
        final String name;
        LocationOption(Long id, String name) { this.id = id; this.name = name; }
        @Override public String toString() { return name; }
    }

    // ==== Utils ====
    private LocalDateTime buildSince() {
        return LocalDateTime.now().minusSeconds(timeoutSeconds);
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
}
