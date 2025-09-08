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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
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

    // NUEVO: soporte colapsable/redimensionable
    @FXML private SplitPane splitMain;
    @FXML private Pane kpiContainer;   // contenedor de los KPIs (top del SplitPane)
    @FXML private Button btnToggleKpis;


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
        refreshNow();

        // Opcional: posición inicial del divisor (30% KPIs / 70% contenido)
        if (splitMain != null && !splitMain.getDividers().isEmpty()) {
            splitMain.getDividers().get(0).setPosition(0.30);
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
                Platform.runLater(() -> refreshNow());
            }
        };
        // cada 5 segundos; ajusta si lo necesitas
        timer.scheduleAtFixedRate(refreshTask, 5000, 5000);
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
        List<LocationPresence> data = dashboard.getPresenceSince(since);

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
        // --- NUEVO: calcular presentes ahora (suma de los contadores por ubicación)
        int presentEmp = data.stream().mapToInt(LocationPresence::getEmployees).sum();
        int presentEqp = data.stream().mapToInt(LocationPresence::getEquipment).sum();
        if (lblPresentEmployees != null) lblPresentEmployees.setText(String.valueOf(presentEmp));
        if (lblPresentEquipment != null) lblPresentEquipment.setText(String.valueOf(presentEqp));


        updateTotals(); // <-- NUEVO
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
        subtitle.setText("Ubicación: " + ubicacionNombre);

        // ⚙️ Caso de uso: trae ocupantes por ubicación
        List<Occupant> oc = dashboard.getOccupantsByUbicacion(ubicacionId);
        List<OccupantRow> rows = new ArrayList<>();
        for (Occupant o : oc) {
            String last = (o.getLastSeen() == null) ? "" : o.getLastSeen().format(TS_FMT);
            rows.add(new OccupantRow(o.getTipo(), o.getEpc(), o.getNombre(), last));
        }
        detailsTable.setItems(FXCollections.observableArrayList(rows));

        // Quitar selección previa
        if (selectedTile != null) {
            selectedTile.getStyleClass().remove("selected");
            selectedTile.setScaleX(1.0);
            selectedTile.setScaleY(1.0);
        }
        // Marcar y animar
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
            if (lblTotalEmployees != null) lblTotalEmployees.setText(String.valueOf(te));
            if (lblTotalEquipment != null) lblTotalEquipment.setText(String.valueOf(tq));
        } catch (Exception ignored) {
            // opcional: loggear si deseas
        }
    }

    @FXML
    private void toggleKpis() {
        if (kpiContainer == null || splitMain == null || splitMain.getDividers().isEmpty()) return;

        SplitPane.Divider divider = splitMain.getDividers().get(0);

        if (kpiContainer.isVisible()) {
            // Guardar posición actual y ocultar
            lastDividerPos = divider.getPosition();
            kpiContainer.setVisible(false);
            kpiContainer.setManaged(false);
            divider.setPosition(0.0); // deja todo el espacio al panel inferior
            if (btnToggleKpis != null) btnToggleKpis.setText("Mostrar KPIs");
        } else {
            // Mostrar y restaurar posición previa o usar un default
            kpiContainer.setVisible(true);
            kpiContainer.setManaged(true);
            divider.setPosition(lastDividerPos != null ? lastDividerPos : 0.30);
            if (btnToggleKpis != null) btnToggleKpis.setText("Ocultar KPIs");
        }
    }


}
