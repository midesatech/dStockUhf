package infrastructure.fx.controller.stock;
import domain.model.Employee;
import domain.model.Equipment;
import domain.model.Ubicacion;
import domain.model.tag.TagScan;
import domain.model.tag.TagScanFilter;
import domain.usecase.EmployeeUseCase;
import domain.usecase.EquipmentUseCase;
import domain.usecase.LocationUseCase;
import domain.usecase.TagUHFUseCase;
import domain.usecase.tag.ScanUseCase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class ScanController {

    @FXML private TextField fEpc;
    @FXML private TextField fLectorCodigo;
    @FXML private ComboBox<Ubicacion> fUbicacion;
    @FXML private DatePicker fDesde;
    @FXML private DatePicker fHasta;
    @FXML private Spinner<Integer> fRssiMin;
    @FXML private Spinner<Integer> fLimit;
    @FXML private TextField fMachine;

    @FXML private TableView<TagScan> table;
    @FXML private TableColumn<TagScan, String> colFecha;
    @FXML private TableColumn<TagScan, String> colLector;
    @FXML private TableColumn<TagScan, String> colUbicacion;
    @FXML private TableColumn<TagScan, String> colEpc;
    @FXML private TableColumn<TagScan, Integer> colRssi;
    @FXML private TableColumn<TagScan, String> colMachine;

    @FXML private Label lblResumen;
    @FXML private Label lblAsignado;

    private final ObservableList<TagScan> data = FXCollections.observableArrayList();

    private final ScanUseCase scanUseCase;
    private final TagUHFUseCase tagUHFUseCase;
    private final EmployeeUseCase employeeUseCase;
    private final EquipmentUseCase equipmentUseCase;
    private final LocationUseCase locationUseCase;

    public ScanController(ScanUseCase scanUseCase,
                          TagUHFUseCase tagUHFUseCase,
                          EmployeeUseCase employeeUseCase,
                          EquipmentUseCase equipmentUseCase,
                          LocationUseCase locationUseCase) {
        this.scanUseCase = scanUseCase;
        this.tagUHFUseCase = tagUHFUseCase;
        this.employeeUseCase = employeeUseCase;
        this.equipmentUseCase = equipmentUseCase;
        this.locationUseCase = locationUseCase;
    }

    @FXML
    public void initialize() {
        fRssiMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-120, 0, -90));
        fLimit.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(50, 5000, 500));

        fUbicacion.getItems().setAll(locationUseCase.listar());
        fUbicacion.setPromptText("Todas");

        // Tabla
        colFecha.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getCreatedAt() != null ? cd.getValue().getCreatedAt().toString() : "")
        );
        colLector.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().getLector() != null ? cd.getValue().getLector().getCodigo() : "")
        );
        colUbicacion.setCellValueFactory(cd ->
                new SimpleStringProperty(
                        cd.getValue().getUbicacion() != null ? cd.getValue().getUbicacion().getNombre()
                                : (cd.getValue().getLector()!=null && cd.getValue().getLector().getUbicacion()!=null
                                ? cd.getValue().getLector().getUbicacion().getNombre() : "")
                )
        );
        colEpc.setCellValueFactory(new PropertyValueFactory<>("epc"));
        colRssi.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().getRssi()));
        colMachine.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getMachine() != null ? cd.getValue().getMachine() : "")
        );

        table.setItems(data);
        table.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> showDetail(b));
        table.setRowFactory(tv -> {
            TableRow<TagScan> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    showFloatingWidget(row.getItem());
                }
            });
            return row;
        });

        refresh();
    }

    @FXML
    public void refresh() {
        TagScanFilter f = new TagScanFilter();
        f.epc = textOrNull(fEpc);
        f.lectorCodigo = textOrNull(fLectorCodigo);
        f.ubicacionId = (fUbicacion.getValue()!=null) ? fUbicacion.getValue().getId() : null;

        LocalDate d = fDesde.getValue();
        LocalDate h = fHasta.getValue();
        if (d != null) f.desde = LocalDateTime.of(d, LocalTime.MIN);
        if (h != null) f.hasta = LocalDateTime.of(h, LocalTime.MAX);

        f.rssiMin = (fRssiMin.getValueFactory()!=null) ? fRssiMin.getValue() : null;
        f.machine = textOrNull(fMachine);
        f.limit = (fLimit.getValueFactory()!=null) ? fLimit.getValue() : 500;

        data.setAll(scanUseCase.buscar(f));
        lblResumen.setText("Registros: " + data.size());
        lblAsignado.setText("");
    }

    @FXML
    public void exportCsv() {
        StringBuilder sb = new StringBuilder("fecha,lector,ubicacion,epc,rssi,machine\n");
        for (TagScan s : data) {
            String fecha = (s.getCreatedAt()!=null) ? s.getCreatedAt().toString() : "";
            String lector = (s.getLector()!=null) ? nonNull(s.getLector().getCodigo()) : "";
            String ubi = s.getUbicacion()!=null ? nonNull(s.getUbicacion().getNombre())
                    : (s.getLector()!=null && s.getLector().getUbicacion()!=null ? nonNull(s.getLector().getUbicacion().getNombre()) : "");
            sb.append(String.join(",",
                    csv(fecha), csv(lector), csv(ubi), csv(nonNull(s.getEpc())),
                    csv(s.getRssi()!=null ? s.getRssi().toString() : ""), csv(nonNull(s.getMachine()))
            )).append("\n");
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);

        new Alert(Alert.AlertType.INFORMATION, "CSV copiado al portapapeles").showAndWait();
    }

    private void showDetail(TagScan s) {
        if (s == null) {
            lblAsignado.setText("");
            return;
        }
        String epc = s.getEpc();
        String ubic = s.getUbicacion()!=null ? s.getUbicacion().getNombre()
                : (s.getLector()!=null && s.getLector().getUbicacion()!=null ? s.getLector().getUbicacion().getNombre() : "—");
        lblResumen.setText("EPC: " + epc + " | Lector: " + (s.getLector()!=null ? s.getLector().getCodigo() : "—")
                + " | Ubicación: " + ubic);

        // Resolver dueño por EPC (Empleado / Equipo)
        Optional<Employee> emp = employeeUseCase.findByEpc(epc);
        if (emp.isPresent()) {
            Employee e = emp.get();
            lblAsignado.setText("Empleado: " + e.getFullName() + " " + e.getLastName());
            return;
        }
        Optional<Equipment> eq = equipmentUseCase.findByEpc(epc);
        if (eq.isPresent()) {
            Equipment e = eq.get();
            lblAsignado.setText("Equipo: " + e.getNombre() + " (SKU: " + nonNull(e.getSku()) + ")");
            return;
        }
        lblAsignado.setText("No asignado");
    }

    private static String textOrNull(TextField tf) {
        return (tf.getText() == null || tf.getText().isBlank()) ? null : tf.getText().trim();
    }
    private static String csv(String s) { return "\"" + s.replace("\"","\"\"") + "\""; }
    private static String nonNull(String s) { return s != null ? s : ""; }

    private void showFloatingWidget(TagScan s) {
        if (s == null) return;

        // Resolver dueño (Empleado/Equipo) igual que en el panel inferior
        String asignado = "No asignado";
        Optional<Employee> emp = employeeUseCase.findByEpc(s.getEpc());
        if (emp.isPresent()) {
            Employee e = emp.get();
            asignado = "Empleado: " + nonNull(e.getFullName()) + " " + nonNull(e.getLastName());
        } else {
            Optional<Equipment> eq = equipmentUseCase.findByEpc(s.getEpc());
            if (eq.isPresent()) {
                Equipment e = eq.get();
                asignado = "Equipo: " + nonNull(e.getNombre()) + (e.getSku()!=null ? " (SKU: " + e.getSku() + ")" : "");
            }
        }

        // Layout del diálogo
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de scan");
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        // Cargar la hoja de estilos (por si el FXML no la cargó aún)
        try {
            String css = getClass().getResource("/infrastructure/fx/css/scan.css").toExternalForm();
            if (!pane.getStylesheets().contains(css)) {
                pane.getStylesheets().add(css);
            }
        } catch (Exception ignore) { /* si no está, no truena */ }

        VBox root = new VBox(12);
        root.getStyleClass().add("floating-card");

        Label title = new Label("Detección UHF");
        title.getStyleClass().add("floating-title");

        // Chips resumen arriba
        HBox chips = new HBox();
        chips.getStyleClass().add("chips");
        chips.getChildren().addAll(
                chip("EPC", "chip-amber"),
                chip(nonNull(s.getEpc()), "chip-rose"),
                chip(s.getLector()!=null ? "Lector: " + nonNull(s.getLector().getCodigo()) : "Lector: —", "chip-blue"),
                chip("RSSI: " + (s.getRssi()!=null ? s.getRssi() : "—"), "chip-green")
        );

        Separator sep1 = new Separator();
        sep1.getStyleClass().add("separator-soft");

        // Grid de key-value
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(4, 0, 0, 0));

        int r = 0;
        grid.add(kvLabel("Fecha/Hora:"), 0, r); grid.add(kvValue(s.getCreatedAt()!=null ? s.getCreatedAt().toString() : "—"), 1, r++);

        String ubic = (s.getUbicacion()!=null ? nonNull(s.getUbicacion().getNombre())
                : (s.getLector()!=null && s.getLector().getUbicacion()!=null ? nonNull(s.getLector().getUbicacion().getNombre()) : "—"));

        grid.add(kvLabel("Ubicación:"), 0, r); grid.add(kvValue(ubic), 1, r++);
        grid.add(kvLabel("Lector:"), 0, r);    grid.add(kvValue(s.getLector()!=null ? nonNull(s.getLector().getCodigo()) : "—"), 1, r++);
        grid.add(kvLabel("EPC:"), 0, r);       grid.add(kvValue(nonNull(s.getEpc())), 1, r++);
        grid.add(kvLabel("RSSI:"), 0, r);      grid.add(kvValue(s.getRssi()!=null ? s.getRssi().toString() : "—"), 1, r++);
        grid.add(kvLabel("Machine:"), 0, r);   grid.add(kvValue(nonNull(s.getMachine())), 1, r++);

        Separator sep2 = new Separator();
        sep2.getStyleClass().add("separator-soft");

        Label asignadoLbl = new Label(asignado);
        asignadoLbl.getStyleClass().add("kv-value");

        root.getChildren().addAll(title, chips, sep1, grid, sep2, asignadoLbl);
        pane.setContent(root);

        dialog.showAndWait();
    }

    private Node kvLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("kv-label");
        return l;
    }

    private Node kvValue(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("kv-value");
        return l;
    }

    private Node chip(String text, String colorClass) {
        Label c = new Label(text);
        c.getStyleClass().addAll("chip", colorClass);
        return c;
    }
}
