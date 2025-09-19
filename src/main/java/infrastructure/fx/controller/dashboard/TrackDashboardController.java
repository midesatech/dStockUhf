package infrastructure.fx.controller.dashboard;

import domain.model.DetectionRecord;
import domain.model.PathHop;
import domain.usecase.tag.SearchDetectionsUseCase;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TrackDashboardController {

    @FXML private Label lblSubtitle, lblRouteInfo;
    @FXML private ToggleGroup grpTipo;
    @FXML private RadioButton rbEmployee, rbEquipment;
    @FXML private ComboBox<QuickRange> cmbQuick;
    @FXML private DatePicker dpStart, dpEnd;
    @FXML private Spinner<Integer> spStartHour, spStartMin, spEndHour, spEndMin;
    @FXML private Button btnSearch, btnReset;

    @FXML private TableView<DetectionRow> tblDetections;
    @FXML private TableColumn<DetectionRow, String> colWhen, colTipo, colEpc, colNombre, colUbic, colDelta;
    @FXML private CheckBox chkZFlow;
    @FXML private ScrollPane routeScroll;
    @FXML private FlowPane routeFlowZ;

    // Keep the last rendered data to re-render on toggle:
    private String currentEpc;
    private List<PathHop> currentHops;
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
        colDelta.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("delta"));

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

        // Make Z-flow wrap to the viewport width of the scroll (minus a little padding)
        routeScroll.viewportBoundsProperty().addListener((obs, oldB, b) -> {
            if (b != null) {
                double pad = 24; // matches -fx-padding 12 on each side
                routeFlowZ.setPrefWrapLength(Math.max(200, b.getWidth() - pad));
            }
        });

        // Toggle between linear and Z flow
        if (chkZFlow != null) {
            chkZFlow.selectedProperty().addListener((o, wasZ, isZ) -> {
                setRouteLayout(isZ);
                // Re-render with current data so the view switches immediately
                rerenderCurrentRoute();
            });
        }

        // Default layout: linear
        setRouteLayout(false);


        updateSubtitle();
    }

    private void setRouteLayout(boolean z) {
        // Only one layout visible/managed at a time
        routeFlow.setVisible(!z);
        routeFlow.setManaged(!z);
        routeFlowZ.setVisible(z);
        routeFlowZ.setManaged(z);
    }

    private void rerenderCurrentRoute() {
        if (currentHops == null || currentHops.isEmpty()) return;
        if (chkZFlow != null && chkZFlow.isSelected()) {
            renderRouteZ(currentEpc, currentHops);
        } else {
            renderRouteLinear(currentEpc, currentHops);
        }
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
            // Original list from repo (likely ordered by fecha DESC)
            var raw = task.getValue();

            // Compute deltas per EPC based on ascending timestamps so the “previous” really is the hop you came from
            var asc = new java.util.ArrayList<DetectionRecord>(raw);
            asc.sort(Comparator
                    .comparing(DetectionRecord::getEpc)
                    .thenComparing(DetectionRecord::getSeenAt));

            Map<String, DetectionRecord> prevByEpc = new HashMap<>();
            Map<DetectionRecord, String> deltaByRec = new HashMap<>();

            for (DetectionRecord r : asc) {
                String deltaStr = "";
                var prev = prevByEpc.get(r.getEpc());
                boolean changedLocation = prev != null && !Objects.equals(prev.getLocationId(), r.getLocationId());
                if (changedLocation && prev.getSeenAt() != null && r.getSeenAt() != null) {
                    var d = Duration.between(prev.getSeenAt(), r.getSeenAt());
                    if (d.isNegative()) d = d.negated();
                    deltaStr = formatDuration(d);
                }
                deltaByRec.put(r, deltaStr);
                prevByEpc.put(r.getEpc(), r);
            }

            // Render in the original order (unchanged UX), but attach the computed deltas
            var rows = new java.util.ArrayList<DetectionRow>(raw.size());
            for (DetectionRecord r : raw) {
                String ts = (r.getSeenAt() == null) ? "" : r.getSeenAt().format(TS_FMT);
                String deltaStr = deltaByRec.getOrDefault(r, "");
                rows.add(new DetectionRow(ts, r.getTipo(), r.getEpc(), r.getNombre(), r.getLocationName(), deltaStr));
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
        currentEpc = epc;
        currentHops = hops;
        if (hops == null || hops.isEmpty()) {
            routeFlow.getChildren().clear();
            routeFlowZ.getChildren().clear();
            lblRouteInfo.setText("Sin datos para EPC " + epc);
            return;
        }
        if (chkZFlow != null && chkZFlow.isSelected()) {
            renderRouteZ(epc, hops);
        } else {
            renderRouteLinear(epc, hops);
        }
    }

    private void renderRouteLinear(String epc, List<PathHop> hops) {
        routeFlow.getChildren().clear();

        StringBuilder summary = new StringBuilder();
        Duration total = Duration.ZERO;

        for (int i = 0; i < hops.size(); i++) {
            PathHop cur = hops.get(i);
            routeFlow.getChildren().add(makeChip(cur.getLocationName(), i));

            if (i < hops.size() - 1) {
                PathHop next = hops.get(i + 1);
                Duration d = null;
                if (cur.getLastSeen() != null && next.getFirstSeen() != null) {
                    d = Duration.between(cur.getLastSeen(), next.getFirstSeen());
                    if (d.isNegative()) d = d.negated();
                }
                String deltaText = formatDuration(d);

                Node arrow = makeArrowWithDelta(deltaText);
                // prevent shrinking
                if (arrow instanceof Region r) keepPrefWidth(r);
                routeFlow.getChildren().add(arrow);

                if (d != null) total = total.plus(d);
                if (deltaText != null && !deltaText.isBlank()) {
                    if (summary.length() > 0) summary.append(", ");
                    summary.append(cur.getLocationName()).append("→").append(next.getLocationName())
                            .append(" ").append(deltaText);
                }
            }
        }

        if (hops.size() >= 2) {
            String totalText = formatDuration(total);
            Node totalBadge = makeTotalTimeBadge(totalText);
            if (totalBadge instanceof Region r) keepPrefWidth(r);
            routeFlow.getChildren().add(totalBadge);
        }

        String info = "EPC " + epc + " • " + hops.size() + " tramos";
        if (summary.length() > 0) info += " • Δ " + summary;
        //lblRouteInfo.setText(info);

        // Compact spacing on very long routes (optional)
        routeFlow.setSpacing(hops.size() > 14 ? 8 : 12);
    }

    private void renderRouteZ(String epc, List<PathHop> hops) {
        routeFlowZ.getChildren().clear();

        StringBuilder summary = new StringBuilder();
        Duration total = Duration.ZERO;

        for (int i = 0; i < hops.size(); i++) {
            PathHop cur = hops.get(i);

            Node chip = makeChip(cur.getLocationName(), i);
            if (chip instanceof Region r1) keepPrefWidth(r1);
            routeFlowZ.getChildren().add(chip);

            if (i < hops.size() - 1) {
                PathHop next = hops.get(i + 1);
                Duration d = null;
                if (cur.getLastSeen() != null && next.getFirstSeen() != null) {
                    d = Duration.between(cur.getLastSeen(), next.getFirstSeen());
                    if (d.isNegative()) d = d.negated();
                }
                String deltaText = formatDuration(d);

                Node arrow = makeArrowWithDelta(deltaText);
                if (arrow instanceof Region r2) keepPrefWidth(r2);
                routeFlowZ.getChildren().add(arrow);

                if (d != null) total = total.plus(d);
                if (deltaText != null && !deltaText.isBlank()) {
                    if (summary.length() > 0) summary.append(", ");
                    summary.append(cur.getLocationName()).append("→").append(next.getLocationName())
                            .append(" ").append(deltaText);
                }
            }
        }

        if (hops.size() >= 2) {
            String totalText = formatDuration(total);
            Node totalBadge = makeTotalTimeBadge(totalText);
            if (totalBadge instanceof Region r3) keepPrefWidth(r3);
            routeFlowZ.getChildren().add(totalBadge);
        }

        String info = "EPC " + epc + " • " + hops.size() + " tramos";
        if (summary.length() > 0) info += " • Δ " + summary;
        //lblRouteInfo.setText(info);
    }


    private Node makeTotalTimeBadge(String totalText) {
        Label badge = new Label("Total Time: " + (totalText == null || totalText.isBlank() ? "—" : totalText));
        badge.getStyleClass().add("total-badge");
        badge.setWrapText(false);

        // NEW: prevent shrinking/cropping
        keepPrefWidth(badge);

        return badge;
    }


    private Node makeArrowWithDelta(String deltaText) {
        Label arrow = new Label("→");
        arrow.getStyleClass().add("route-arrow");
        arrow.setWrapText(false);

        Label badge = new Label((deltaText == null || deltaText.isBlank()) ? "Δ —" : "Δ " + deltaText);
        badge.getStyleClass().add("delta-badge");
        badge.setWrapText(false);

        VBox box = new VBox(2, arrow, badge);
        box.setAlignment(Pos.CENTER);

        // NEW: prevent shrinking/cropping
        keepPrefWidth(arrow);
        keepPrefWidth(badge);
        keepPrefWidth(box);

        return box;
    }


    /** Keep nodes at their preferred width; never shrink or stretch in routeFlow. */
    private static void keepPrefWidth(Region... rs) {
        for (Region r : rs) {
            if (r == null) continue;
            r.setMinWidth(Region.USE_PREF_SIZE);
            r.setMaxWidth(Region.USE_PREF_SIZE);
            if (r.getParent() instanceof HBox) {
                HBox.setHgrow(r, Priority.NEVER);
            }
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

    private String formatDuration(Duration d) {
        long s = Math.abs(d.getSeconds());
        long hrs = s / 3600; s %= 3600;
        long mins = s / 60; long secs = s % 60;
        if (hrs > 0) return String.format("%dh %02dm %02ds", hrs, mins, secs);
        if (mins > 0) return String.format("%dm %02ds", mins, secs);
        return String.format("%ds", secs);
    }


    public static class DetectionRow {
        private final String when, tipo, epc, nombre, ubicacion, delta;
        public DetectionRow(String when, String tipo, String epc, String nombre, String ubicacion, String delta) {
            this.when = when; this.tipo = tipo; this.epc = epc; this.nombre = nombre; this.ubicacion = ubicacion;   this.delta = delta;
        }
        public String getWhen() { return when; }
        public String getTipo() { return tipo; }
        public String getEpc() { return epc; }
        public String getNombre() { return nombre; }
        public String getUbicacion() { return ubicacion; }
        public String getDelta() { return delta; }
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