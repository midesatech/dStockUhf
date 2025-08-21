package infrastructure.fx.controller.stock;

import domain.model.TagUHF;
import domain.usecase.TagUHFUseCase;
//import infrastructure.serial.UHFReaderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TagUHFController {

    @FXML private TextField txtEpc;
    @FXML private ComboBox<TagUHF.Tipo> cmbTipo;
    @FXML private CheckBox chkActivo;
    @FXML private TableView<TagUHF> tabla;
    @FXML private TableColumn<TagUHF, Long> colId;
    @FXML private TableColumn<TagUHF, String> colEpc;
    @FXML private TableColumn<TagUHF, TagUHF.Tipo> colTipo;
    @FXML private TableColumn<TagUHF, Boolean> colActivo;

    private final ObservableList<TagUHF> data = FXCollections.observableArrayList();
    private final TagUHFUseCase useCase;

    // servicio que conecta al puerto serial del lector UHF
    //private final UHFReaderService readerService;

    private TagUHF seleccionado;

    public TagUHFController(TagUHFUseCase useCase) {//, UHFReaderService readerService) {
        this.useCase = useCase;
       // this.readerService = readerService;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getId()));
        colEpc.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getEpc()));
        colTipo.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getTipo()));
        colActivo.setCellValueFactory(c -> new javafx.beans.property.SimpleBooleanProperty(c.getValue().isActivo()));

        cmbTipo.setItems(FXCollections.observableArrayList(TagUHF.Tipo.values()));
        tabla.setItems(data);

        refresh();
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                seleccionado = newSel;
                txtEpc.setText(newSel.getEpc());
                cmbTipo.setValue(newSel.getTipo());
                chkActivo.setSelected(newSel.isActivo());
            }
        });
    }

    @FXML
    public void nuevo() {
        seleccionado = null;
        txtEpc.clear();
        cmbTipo.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
    }

    @FXML
    public void guardar() {
        String epc = txtEpc.getText();
        TagUHF.Tipo tipo = cmbTipo.getValue();
        boolean activo = chkActivo.isSelected();

        if (epc == null || epc.isBlank() || tipo == null) {
            new Alert(Alert.AlertType.WARNING, "Debe ingresar EPC y seleccionar Tipo.").show();
            return;
        }

        TagUHF tag = (seleccionado == null)
                ? new TagUHF(null, epc, tipo, activo)
                : new TagUHF(seleccionado.getId(), epc, tipo, activo);

        if (seleccionado == null) {
            useCase.save(tag);
        } else {
            useCase.update(tag);
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
    public void detectarTag() {
        try {
            String epcDetectado = "";//readerService.readEPC(); // obtiene EPC del lector serial
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
