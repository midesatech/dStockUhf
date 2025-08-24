package infrastructure.fx.controller.stock;

import app.config.AppConfig;
import domain.model.Empleado;
import domain.model.Equipment;
import domain.model.TagUHF;
import domain.model.tag.ESerialMode;
import domain.model.tag.ErrorCode;
import domain.model.tag.ReadWriteResult;
import domain.model.tag.RxDto;
import domain.usecase.EmpleadoUseCase;
import domain.usecase.EquipmentUseCase;
import domain.usecase.TagUHFUseCase;
import domain.usecase.tag.ReadTagUseCase;
import infrastructure.fx.controller.MainController;
import infrastructure.logging.LogMessages;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
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
    @FXML private TableColumn<TagUHF, String> colAsignado;

    private final ObservableList<TagUHF> data = FXCollections.observableArrayList();
    private final TagUHFUseCase useCase;
    private final EmpleadoUseCase empleadoUseCase;
    private final EquipmentUseCase equipmentUseCase;
    private final ReadTagUseCase readTagUseCase;

    private TagUHF seleccionado;

    private boolean isBackGroundActivate = false;
    private static final String ACTIVATE = "Activar    ";
    private static final String DEACTIVATE = "Activado ";

    private static final Logger logger = LogManager.getLogger(TagUHFController.class);

    public TagUHFController(TagUHFUseCase useCase,
                            EmpleadoUseCase empleadoUseCase,
                            EquipmentUseCase equipmentUseCase,
                            ReadTagUseCase readTagUseCase) {
        this.useCase = useCase;
        this.empleadoUseCase = empleadoUseCase;
        this.equipmentUseCase = equipmentUseCase;
        this.readTagUseCase = readTagUseCase;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        colEpc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEpc()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTipo()));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActivo()));
        colAsignado.setCellValueFactory(c -> {
            TagUHF tag = c.getValue();
            String epc = tag.getEpc();
            String display = "";
            try {
                if (tag.getTipo() == TagUHF.Tipo.EMPLEADO) {
                    display = empleadoUseCase.findByEpc(epc)
                            .map(emp -> emp.getFullName() + " " + emp.getLastName())
                            .orElse("");
                } else if (tag.getTipo() == TagUHF.Tipo.EQUIPMENT) {
                    display = equipmentUseCase.findByEpc(epc)
                            .map(eq -> {
                                String sku = (eq.getSku() == null ? "" : eq.getSku());
                                String nombre = (eq.getNombre() == null ? "" : eq.getNombre());
                                String sep = (!sku.isBlank() && !nombre.isBlank()) ? " - " : "";
                                return sku + sep + nombre;
                            })
                            .orElse("");
                }
            } catch (Exception ex) {
                // swallow and show empty to keep the table robust
                display = "";
            }
            return new javafx.beans.property.SimpleStringProperty(display);
        });


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
                // 🔹 Seleccionar en cmbAsignacion según el tipo
                if (newSel.getTipo() == TagUHF.Tipo.EMPLEADO) {
                    cmbAsignacion.setItems(FXCollections.observableArrayList(empleadoUseCase.listar()));
                    empleadoUseCase.findByEpc(newSel.getEpc()).ifPresent(emp -> cmbAsignacion.setValue(emp));
                } else if (newSel.getTipo() == TagUHF.Tipo.EQUIPMENT) {
                    cmbAsignacion.setItems(FXCollections.observableArrayList(equipmentUseCase.listar()));
                    equipmentUseCase.findByEpc(newSel.getEpc()).ifPresent(eq -> cmbAsignacion.setValue(eq));
                } else {
                    cmbAsignacion.getItems().clear();
                }

            }
        });

        refresh();
        setReadTagUseCaseProperties();
    }

    private void setReadTagUseCaseProperties() {
        this.readTagUseCase.messageProperty()
                .addListener((observable, oldMessage, newMessage) -> {
                    new Alert(Alert.AlertType.INFORMATION, newMessage).show();
        });

        this.readTagUseCase.setOnSucceeded(event -> {
            ReadWriteResult result = readTagUseCase.getValue(); // <-- result from createTask()
            System.out.println("Tag read result: " + result);

            if (Objects.nonNull(result)) {
                switch (result.getErrorCode()) {
                    case OK:
                        this.txtEpc.setText(result.getData().getHexEpc());
                        break;
                }
                if (result.isShowMessage()) {
                    new Alert(Alert.AlertType.INFORMATION, result.getMessage()).show();
                }
            }
            deactivateBackgroundTagDetection();

        });

        // Optional: attach error handler
        this.readTagUseCase.setOnFailed(event -> {
            Throwable ex = readTagUseCase.getException();
            ex.printStackTrace();
        });
    }

    @FXML
    public void nuevo() {
        seleccionado = null;
        txtEpc.clear();
        cmbTipo.getSelectionModel().clearSelection();
        cmbAsignacion.getSelectionModel().clearSelection();
        cmbAsignacion.getItems().clear();
        cmbAsignacion.setValue(null);
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
        handleTagDetection();
    }

    public void handleTagDetection() {
            if (!isBackGroundActivate) {
                setSaveAction(DEACTIVATE, false);
                activateBackgroundTagDetection();
            } else {
                setSaveAction(ACTIVATE, true);
                deactivateBackgroundTagDetection();
            }
     }

    private void setSaveAction(String message, boolean activate) {
        MainController.getInstance().setTagDetectionStatus(message);
        isBackGroundActivate = !activate;
    }

    private void deactivateBackgroundTagDetection() {
        Worker.State currentState = readTagUseCase.getState();
        if (currentState == Worker.State.RUNNING) {
            readTagUseCase.cancel();
        }
        setSaveAction(ACTIVATE, true);
        txtEpc.getScene().setCursor(Cursor.DEFAULT);
    }

    private void activateBackgroundTagDetection() {
        //poneMensaje("Detección activada: Esperando TAG");
        txtEpc.getScene().setCursor(Cursor.WAIT);
        startTagDetection(1L);
    }

    public void startTagDetection(Long order) {
        if (readTagUseCase.getSerialMode() == ESerialMode.NO) {
            setSaveAction(ACTIVATE, true);
            deactivateBackgroundTagDetection();
            new Alert(Alert.AlertType.ERROR, "Error: Lector no seleccionado").show();
            return;
        }
        findTagTask(order);
    }

    private void findTagTask(Long numOrder) {
        Worker.State currentState = readTagUseCase.getState();
        if (currentState == Worker.State.READY) {
            logger.info(LogMessages.fromComponent("TagUHFController", LogMessages.EPC_READ_STARTED));
            readTagUseCase.start();
        } else if (currentState == Worker.State.RUNNING) {
            logger.info(LogMessages.fromComponent("TagUHFController", LogMessages.TASK_ALREADY_RUNNING));
        } else {
            logger.info(LogMessages.fromComponent("TagUHFController", LogMessages.TASK_RESTARTING));
            readTagUseCase.restart();
        }
    }

    private void processResult(ReadWriteResult result) {
        if (Objects.nonNull(result) && result.isShowMessage()) {
            new Alert(Alert.AlertType.ERROR, result.getMessage()).show();
        }
        setSaveAction(ACTIVATE, true);
        if (result.getErrorCode().equals(ErrorCode.OK)) {
            //this.txtIdOrdenFabricacion.setText("");
            //startNotification();
        }
    }

    private boolean isValidRx(RxDto rxDto) {
        return Objects.nonNull(rxDto) && rxDto.getIsValid();
    }

}