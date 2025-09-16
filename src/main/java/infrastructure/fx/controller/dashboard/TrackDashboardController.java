package infrastructure.fx.controller.dashboard;

import domain.model.DetectionRecord;
import domain.model.PathHop;
import domain.usecase.tag.SearchDetectionsUseCase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrackDashboardController {

    @FXML private Label lblSubtitle, lblRouteInfo;
    @FXML private ToggleGroup grpTipo;
    @FXML private RadioButton rbEmployee, rbEquipment;
    @FXML private ComboBox<QuickRange> cmbQuick;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private Spinner<Integer> spStartHour, spStartMin, spEndHour, spEndMin;
    @FXML private Button btnSearch, btnReset;

    @FXML private TableView<DetectionRow> tblDetections;
    @FXML private TableColumn<DetectionRow, String> colWhen, colTipo, colEpc, colNombre, colUbic;

    @FXML private HBox routeFlow;

    private final SearchDetectionsUseCase useCase;
    private final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TrackDashboardController(SearchDetectionsUseCase useCase) {
        this.useCase = useCase;
    }

    @FXML
    public void initialize() {
        colWhen.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("when"));
        colTipo.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tipo"));
        colEpc.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("epc"));
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colUbic.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("ubicacion"));

        tblDetections.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(DetectionRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-employee", "row-equipment");
                if (empty || item == null) return;
                String t = item.getTipo() == null ? "" : item.getTipo().toUpperCase();
                if (t.contains("EMP")) getStyleClass().add("row-employee");
                else getStyleClass().add("row-equipment");
            }
        });

        tblDetections.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
            if (nv == null) clearRoute();
            else buildRouteAsync(nv.getEpc());
        });

        setupSpinner(spStartHour, 0, 23);
        setupSpinner(spStartMin,  0, 59);
        setupSpinner(spEndHour,   0, 23);
        setupSpinner(spEndMin,    0, 59);

        if (grpTipo != null && grpTipo.getSelectedToggle() == null && rbEmployee != null) {
            rbEmployee.setSelected(true);
        }

        cmbQuick.setConverter(new StringConverter<>() {
            @Override public String toString(QuickRange q) { return q == null ? "" : q.label; }
            @Override public QuickRange fromString(String s) { return null; }
        });
        cmbQuick.getItems().setAll(QuickRange.defaults());
        cmbQuick.getSelectionModel().select(QuickRange.LAST_30M);
        applyQuickRange(cmbQuick.getValue());
        cmbQuick.valueProperty().addListener((o, old, q) -> applyQuickRange(q));

        updateSubtitle();
    }

    private void setupSpinner(Spinner<Integer> sp, int min, int max) {
        sp.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, 0));
        sp.setEditable(true);
    }

    private void applyQuickRange(QuickRange q) {
        if (q == null) return;
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = q.apply(end);

        dpStart.setValue(start.toLocalDate());
        spStartHour.getValueFactory().setValue(start.getHour());
        spStartMin.getValueFactory().setValue(start.getMinute());

        dpEnd.setValue(end.toLocalDate());
        spEndHour.getValueFactory().setValue(end.getHour());
        spEndMin.getValueFactory().setValue(end.getMinute());

        updateSubtitle();
    }

    private LocalDateTime buildStart() {
        LocalDate d = dpStart.getValue() != null ? dpStart.getValue() : LocalDate.now();
        int h = spStartHour.getValue() != null ? spStartHour.getValue() : 0;
        int m = spStartMin.getValue() != null ? spStartMin.getValue() : 0;
        return LocalDateTime.of(d, LocalTime.of(h, m));
    }
    private LocalDateTime buildEnd() {
        LocalDate d = dpEnd.getValue() != null ? dpEnd.getValue() : LocalDate.now();
        int h = spEndHour.getValue() != null ? spEndHour.getValue() : 23;
        int m = spEndMin.getValue() != null ? spEndMin.getValue() : 59;
        return LocalDateTime.of(d, LocalTime.of(h, m));
    }

    @FXML public void onSearch() {
        String subject = rbEquipment.isSelected() ? "EQUIPMENT" : "EMPLOYEE";
        LocalDateTime start = buildStart();
        LocalDateTime end   = buildEnd();
        updateSubtitle();

        javafx.concurrent.Task<List<DetectionRecord>> task = new javafx.concurrent.Task<>() {
            @Override protected List<DetectionRecord> call() {
                return useCase.searchRaw(subject, start, end);
            }
        };
        task.setOnSucceeded(e -> {
            List<DetectionRow> rows = new ArrayList<>();
            for (DetectionRecord r : task.getValue()) {
                String ts = r.getSeenAt() == null ? "" : r.getSeenAt().format(TS_FMT);
                rows.add(new DetectionRow(ts, r.getTipo(), r.getEpc(), r.getNombre(), r.getLocationName()));
            }
            tblDetections.setItems(FXCollections.observableArrayList(rows));
            clearRoute();
        });
        task.setOnFailed(e -> {
            tblDetections.getItems().clear();
            clearRoute();
        });
        Thread t = new Thread(task, "track-search");
        t.setDaemon(true);
        t.start();
    }

    @FXML public void onReset() {
        rbEmployee.setSelected(true);
        cmbQuick.getSelectionModel().select(QuickRange.LAST_30M);
        applyQuickRange(cmbQuick.getValue());
        tblDetections.getItems().clear();
        clearRoute();
        updateSubtitle();
    }

    private void updateSubtitle() {
        String subject = rbEquipment.isSelected() ? "Equipment" : "Employee";
        LocalDateTime start = buildStart();
        LocalDateTime end   = buildEnd();
        lblSubtitle.setText("Tipo: " + subject + "  •  Rango: " + start.format(TS_FMT) + " → " + end.format(TS_FMT));
    }

    private void buildRouteAsync(String epc) {
        if (epc == null || epc.isBlank()) { clearRoute(); return; }
        LocalDateTime start = buildStart();
        LocalDateTime end   = buildEnd();

        javafx.concurrent.Task<List<PathHop>> task = new javafx.concurrent.Task<>() {
            @Override protected List<PathHop> call() {
                return useCase.routeForEpc(epc, start, end);
            }
        };
        task.setOnSucceeded(e -> renderRoute(epc, task.getValue()));
        task.setOnFailed(e -> clearRoute());
        Thread t = new Thread(task, "track-route");
        t.setDaemon(true);
        t.start();
    }

    private void clearRoute() {
        routeFlow.getChildren().clear();
        lblRouteInfo.setText("");
    }

    private void renderRoute(String epc, List<PathHop> hops) {
        routeFlow.getChildren().clear();
        if (hops == null || hops.isEmpty()) {
            lblRouteInfo.setText("Sin datos para EPC " + epc);
            return;
        }
        lblRouteInfo.setText("EPC " + epc + " • " + hops.size() + " tramos");

        for (int i = 0; i < hops.size(); i++) {
            PathHop h = hops.get(i);
            routeFlow.getChildren().add(makeChip(h.getLocationName(), i));
            if (i < hops.size() - 1) routeFlow.getChildren().add(makeArrow());
        }
    }

    private Node makeChip(String text, int idx) {
        StackPane chip = new StackPane();
        chip.getStyleClass().addAll("route-chip", "chip-" + (idx % 6));
        chip.setMinWidth(120);
        chip.setMinHeight(48);
        chip.setMaxWidth(220);
        chip.setPrefHeight(48);
        Text t = new Text(text == null ? "(?)" : text);
        t.getStyleClass().add("route-chip-text");
        chip.getChildren().add(t);
        return chip;
    }

    private Node makeArrow() {
        Label arrow = new Label("→");
        arrow.getStyleClass().add("route-arrow");
        arrow.setMinWidth(18);
        return arrow;
    }

    public static class DetectionRow {
        private final String when, tipo, epc, nombre, ubicacion;
        public DetectionRow(String when, String tipo, String epc, String nombre, String ubicacion) {
            this.when = when; this.tipo = tipo; this.epc = epc; this.nombre = nombre; this.ubicacion = ubicacion;
        }
        public String getWhen() { return when; }
        public String getTipo() { return tipo; }
        public String getEpc() { return epc; }
        public String getNombre() { return nombre; }
        public String getUbicacion() { return ubicacion; }
    }

    public static class QuickRange {
        public static final QuickRange LAST_30M = new QuickRange("Últimos 30 min", 0, 30);
        public static final QuickRange LAST_1H  = new QuickRange("Última 1 hora", 1, 0);
        public static final QuickRange LAST_2H  = new QuickRange("Últimas 2 horas", 2, 0);
        public static final QuickRange LAST_3H  = new QuickRange("Últimas 3 horas", 3, 0);
        public static final QuickRange LAST_5H  = new QuickRange("Últimas 5 horas", 5, 0);
        public static final QuickRange LAST_8H  = new QuickRange("Últimas 8 horas", 8, 0);

        final String label; final int hours; final int minutes;
        public QuickRange(String label, int hours, int minutes) {
            this.label = label; this.hours = hours; this.minutes = minutes;
        }
        public LocalDateTime apply(LocalDateTime end) { return end.minusHours(hours).minusMinutes(minutes); }
        public static List<QuickRange> defaults() {
            return List.of(LAST_30M, LAST_1H, LAST_2H, LAST_3H, LAST_5H, LAST_8H);
        }
    }
}