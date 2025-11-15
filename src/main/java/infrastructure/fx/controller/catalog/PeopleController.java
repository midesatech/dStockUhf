
package infrastructure.fx.controller.catalog;

import domain.exception.DuplicateFieldException;
import domain.model.People;
import domain.model.PeopleType;
import domain.model.TipoDocumento;
import domain.model.TipoSangre;
import domain.usecase.PeopleUseCase;
import domain.usecase.PeopleTypeUseCase;
import infrastructure.fx.component.YearPickerDate;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class PeopleController {
    @FXML
    private TableView<People> tbl;
    @FXML
    private TableColumn<People, Long> colId;
    @FXML
    private TableColumn<People, PeopleType> colPeopleId;
    @FXML
    private TableColumn<People, String> colCodigo;
    @FXML
    private TableColumn<People, String> colNombre;
    @FXML
    private TableColumn<People, String> colApellido;
    @FXML
    private TableColumn<People, TipoDocumento> colDocType;
    @FXML
    private TableColumn<People, String> colNumDoc;
    @FXML
    private TableColumn<People, LocalDate> colNacimiento;
    @FXML
    private TableColumn<People, TipoSangre> colBlood;
    @FXML
    private TableColumn<People, String> colEmail;
    @FXML
    private TableColumn<People, String> colTelefono;

    @FXML
    private TextField txtCodigo;
    @FXML
    private ComboBox<PeopleType> cmbPeopleType;
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
    @FXML
    private ComboBox<PeopleType> filtroTipoPersona;

    private final ObservableList<People> data = FXCollections.observableArrayList();
    private final PeopleUseCase useCase;
    private final PeopleTypeUseCase peopleTypeUseCase;

    // Inyectado vía ControllerFactory/AppBootstrap
    public PeopleController(PeopleUseCase useCase, PeopleTypeUseCase peopleTypeUseCase) {
        this.useCase = useCase;
        this.peopleTypeUseCase = peopleTypeUseCase;
    }

    @FXML
    public void initialize() {
        // Combos
        cmbDocType.setItems(FXCollections.observableArrayList(TipoDocumento.values()));
        cmbBloodType.setItems(FXCollections.observableArrayList(TipoSangre.values()));
        cmbPeopleType.setItems(FXCollections.observableArrayList(peopleTypeUseCase.listar()));
        filtroDocType.setItems(FXCollections.observableArrayList(TipoDocumento.values()));
        filtroTipoPersona.setItems(FXCollections.observableArrayList(peopleTypeUseCase.listar()));
        // Tabla
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPeopleId.setCellValueFactory(new PropertyValueFactory<>("peopleType"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("epc"));
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

    private void loadForm(People e) {
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
        cmbPeopleType.setValue(e.getPeopleType());
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    @FXML
    public void nuevo() {
        tbl.getSelectionModel().clearSelection();
        txtCodigo.clear();
        cmbPeopleType.getSelectionModel().clearSelection();
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
            People sel = tbl.getSelectionModel().getSelectedItem();
            Long id = sel != null ? sel.getId() : null;

            People e = new People();
            e.setId(id);
            e.setPeopleType(cmbPeopleType.getValue());
            e.setEpc(blankToNull(txtCodigo.getText()));
            e.setFullName(txtNombre.getText());
            e.setLastName(txtApellido.getText());
            e.setDocType(cmbDocType.getValue());
            e.setDocNumber(txtNumDoc.getText());
            e.setBirthDate(dpNacimiento.getValue());
            e.setBloodType(cmbBloodType.getValue());
            e.setEmail(blankToNull(txtEmail.getText()));
            e.setPhone(blankToNull(txtTelefono.getText()));

            People saved = useCase.save(e);

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
        People sel = tbl.getSelectionModel().getSelectedItem();
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
                filtroTipoPersona.getValue(),
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
        filtroTipoPersona.getSelectionModel().clearSelection();
        tbl.getItems().setAll(useCase.listar()); // volver a cargar todo
    }
}
