package infrastructure.fx.controller.stock;

import domain.model.Empleado;
import domain.model.Equipment;
import domain.model.TagUHF;
import domain.usecase.EmpleadoUseCase;
import domain.usecase.EquipmentUseCase;
import domain.usecase.TagUHFUseCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Optional;

public class TagUHFController {

    @FXML private TextField txtEpc;
    @FXML private ComboBox<TagUHF.Tipo> cmbTipo;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<TagUHF> tabla;
    @FXML private TableColumn<TagUHF, Long> colId;
    @FXML private TableColumn<TagUHF, String> colEpc;
    @FXML private TableColumn<TagUHF, TagUHF.Tipo> colTipo;
    @FXML private TableColumn<TagUHF, Boolean> colActivo;
    @FXML private ComboBox<Object> cmbAsignacion;
    @FXML private TextField txtFiltroEpc;

    private final ObservableList<TagUHF> data = FXCollections.observableArrayList();
    private final TagUHFUseCase useCase;
    private final EmpleadoUseCase empleadoUseCase;
    private final EquipmentUseCase equipmentUseCase;

    private TagUHF seleccionado;

    public TagUHFController(TagUHFUseCase useCase,
                            EmpleadoUseCase empleadoUseCase,
                            EquipmentUseCase equipmentUseCase) {
        this.useCase = useCase;
        this.empleadoUseCase = empleadoUseCase;
        this.equipmentUseCase = equipmentUseCase;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        colEpc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEpc()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTipo()));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActivo()));

        cmbTipo.setItems(FXCollections.observableArrayList(TagUHF.Tipo.values()));
        tabla.setItems(data);

        cmbTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == TagUHF.Tipo.EMPLEADO) {
                cmbAsignacion.setItems(FXCollections.observableArrayList(empleadoUseCase.listar()));
            } else if (newVal == TagUHF.Tipo.EQUIPMENT) {
                cmbAsignacion.setItems(FXCollections.observableArrayList(equipmentUseCase.listar()));
            } else {
                cmbAsignacion.getItems().clear();
            }
        });

        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionado = newSel;
                txtEpc.setText(newSel.getEpc());
                cmbTipo.setValue(newSel.getTipo());
                chkActivo.setSelected(newSel.isActivo());
            }
        });

        refresh();
    }

    @FXML
    public void nuevo() {
        seleccionado = null;
        txtEpc.clear();
        cmbTipo.getSelectionModel().clearSelection();
        cmbAsignacion.getItems().clear();
        chkActivo.setSelected(true);
    }

    @FXML
    public void guardar() {
        String epc = txtEpc.getText();
        TagUHF.Tipo tipo = cmbTipo.getValue();
        Object asignacion = cmbAsignacion.getValue();
        boolean activo = chkActivo.isSelected();

        if (epc == null || epc.isBlank() || tipo == null || asignacion == null) {
            new Alert(Alert.AlertType.WARNING, "Debe ingresar EPC, seleccionar Tipo y asignación.").show();
            return;
        }

        // Validar si ya existe EPC
        if (useCase.findByEpc(epc).isPresent() && seleccionado == null) {
            new Alert(Alert.AlertType.ERROR, "El EPC ya está asignado a otro registro.").show();
            return;
        }

        TagUHF tag = (seleccionado == null)
                ? new TagUHF(null, epc, tipo, activo)
                : new TagUHF(seleccionado.getId(), epc, tipo, activo);

        // Persistir el Tag
        TagUHF saved = (seleccionado == null) ? useCase.save(tag) : useCase.update(tag);

// Asignación según tipo
        if (tipo == TagUHF.Tipo.EMPLEADO) {
            empleadoUseCase.asignarEpc(((Empleado) asignacion).getId(), epc);
        } else {
            equipmentUseCase.asignarEpc(((Equipment) asignacion).getId(), epc);
        }

        refresh();
        nuevo();
    }

    @FXML
    public void eliminar() {
        if (seleccionado != null) {
            useCase.deleteById(seleccionado.getId());
            refresh();
            nuevo();
        }
    }

    @FXML
    public void refresh() {
        data.setAll(useCase.findAll());
    }

    @FXML
    public void buscarEpc() {
        String filtro = txtFiltroEpc.getText();
        if (filtro != null && !filtro.isBlank()) {
            Optional<TagUHF> encontrado = useCase.findByEpc(filtro);
            if (encontrado.isPresent()) {
                tabla.getSelectionModel().select(encontrado.get());
                tabla.scrollTo(encontrado.get());
            } else {
                new Alert(Alert.AlertType.INFORMATION, "No se encontró TAG con EPC: " + filtro).show();
            }
        } else {
            refresh();
        }
    }

    @FXML
    public void detectarTag() {
        try {
            String epcDetectado = "";// readerService.readEPC();
            if (epcDetectado != null && !epcDetectado.isBlank()) {
                txtEpc.setText(epcDetectado);
                new Alert(Alert.AlertType.INFORMATION, "TAG detectado: " + epcDetectado).show();
            } else {
                new Alert(Alert.AlertType.WARNING, "No se detectó ningún TAG").show();
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error leyendo TAG: " + e.getMessage()).show();
        }
    }
}