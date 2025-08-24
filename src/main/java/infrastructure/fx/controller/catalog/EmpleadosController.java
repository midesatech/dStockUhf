
package infrastructure.fx.controller.catalog;

import domain.exception.DuplicateFieldException;
import domain.model.Empleado;
import domain.model.TipoDocumento;
import domain.model.TipoSangre;
import domain.usecase.EmpleadoUseCase;
import infrastructure.fx.component.YearPickerDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class EmpleadosController {
    @FXML
    private TableView<Empleado> tbl;
    @FXML
    private TableColumn<Empleado, Long> colId;
    @FXML
    private TableColumn<Empleado, String> colCodigo;
    @FXML
    private TableColumn<Empleado, String> colNombre;
    @FXML
    private TableColumn<Empleado, String> colApellido;
    @FXML
    private TableColumn<Empleado, TipoDocumento> colDocType;
    @FXML
    private TableColumn<Empleado, String> colNumDoc;
    @FXML
    private TableColumn<Empleado, LocalDate> colNacimiento;
    @FXML
    private TableColumn<Empleado, TipoSangre> colBlood;
    @FXML
    private TableColumn<Empleado, String> colEmail;
    @FXML
    private TableColumn<Empleado, String> colTelefono;

    @FXML
    private TextField txtCodigo;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtApellido;
    @FXML
    private ComboBox<TipoDocumento> cmbDocType;
    @FXML
    private TextField txtNumDoc;
    @FXML
    private YearPickerDate dpNacimiento;
    @FXML
    private ComboBox<TipoSangre> cmbBloodType;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField filtroCodigo;
    @FXML
    private TextField filtroNombre;
    @FXML
    private TextField filtroApellido;
    @FXML
    private ComboBox<TipoDocumento> filtroDocType;
    @FXML
    private TextField filtroNumDoc;

    private final ObservableList<Empleado> data = FXCollections.observableArrayList();
    private final EmpleadoUseCase useCase;

    // Inyectado vía ControllerFactory/AppBootstrap
    public EmpleadosController(EmpleadoUseCase useCase) {
        this.useCase = useCase;
    }

    @FXML
    public void initialize() {
        // Combos
        cmbDocType.setItems(FXCollections.observableArrayList(TipoDocumento.values()));
        cmbBloodType.setItems(FXCollections.observableArrayList(TipoSangre.values()));
        filtroDocType.setItems(FXCollections.observableArrayList(TipoDocumento.values()));
        // Tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colDocType.setCellValueFactory(new PropertyValueFactory<>("docType"));
        colNumDoc.setCellValueFactory(new PropertyValueFactory<>("docNumber"));
        colNacimiento.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colBlood.setCellValueFactory(new PropertyValueFactory<>("bloodType"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("phone"));

        tbl.setItems(data);
        refresh();

        // Selección → carga en formulario
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> loadForm(sel));
    }

    private void loadForm(Empleado e) {
        if (e == null) {
            nuevo();
            return;
        }
        txtCodigo.setText(nullToEmpty(e.getEpc()));
        txtNombre.setText(nullToEmpty(e.getFullName()));
        txtApellido.setText(nullToEmpty(e.getLastName()));
        cmbDocType.setValue(e.getDocType());
        txtNumDoc.setText(nullToEmpty(e.getDocNumber()));
        dpNacimiento.setValue(e.getBirthDate());
        cmbBloodType.setValue(e.getBloodType());
        txtEmail.setText(nullToEmpty(e.getEmail()));
        txtTelefono.setText(nullToEmpty(e.getPhone()));
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    public void nuevo() {
        tbl.getSelectionModel().clearSelection();
        txtCodigo.clear();
        txtNombre.clear();
        txtApellido.clear();
        cmbDocType.getSelectionModel().clearSelection();
        txtNumDoc.clear();
        dpNacimiento.setValue(null);
        cmbBloodType.getSelectionModel().clearSelection();
        txtEmail.clear();
        txtTelefono.clear();
    }

    @FXML
    public void guardar() {
        try {
            Empleado sel = tbl.getSelectionModel().getSelectedItem();
            Long id = sel != null ? sel.getId() : null;

            Empleado e = new Empleado();
            e.setId(id);
            e.setEpc(blankToNull(txtCodigo.getText()));
            e.setFullName(txtNombre.getText());
            e.setLastName(txtApellido.getText());
            e.setDocType(cmbDocType.getValue());
            e.setDocNumber(txtNumDoc.getText());
            e.setBirthDate(dpNacimiento.getValue());
            e.setBloodType(cmbBloodType.getValue());
            e.setEmail(blankToNull(txtEmail.getText()));
            e.setPhone(blankToNull(txtTelefono.getText()));

            Empleado saved = useCase.save(e);

            if (id == null) data.add(saved); // si es nuevo, añade a la tabla
            refresh(); // asegura sincronía
            show("Guardado");
            // Opcional: volver a seleccionar el registro
            tbl.getSelectionModel().select(saved);
        } catch (DuplicateFieldException ex) {
            show("Error", ex.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception ex) {
            show(ex.getMessage());
        }
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    @FXML
    public void eliminar() {
        Empleado sel = tbl.getSelectionModel().getSelectedItem();
        if (sel == null) {
            show("Seleccione un empleado");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar empleado seleccionado?", ButtonType.OK, ButtonType.CANCEL);
        confirm.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                try {
                    useCase.eliminar(sel.getId());
                    data.remove(sel);
                    nuevo();
                } catch (Exception ex) {
                    show(ex.getMessage());
                }
            }
        });
    }

    @FXML
    public void refresh() {
        data.clear();
        data.setAll(useCase.listar());
    }

    private void show(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }

    private void show(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @FXML
    private void buscar() {
        var results = useCase.buscar(
                filtroDocType.getValue(),
                filtroNumDoc.getText(),
                filtroNombre.getText(),
                filtroApellido.getText(),
                filtroCodigo.getText()
        );
        tbl.getItems().setAll(results);
    }

    @FXML
    private void limpiarFiltros() {
        filtroCodigo.clear();
        filtroNombre.clear();
        filtroApellido.clear();
        filtroDocType.getSelectionModel().clearSelection();
        filtroNumDoc.clear();
        tbl.getItems().setAll(useCase.listar()); // volver a cargar todo
    }
}
