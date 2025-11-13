package infrastructure.fx.controller.catalog;

import domain.model.PeopleType;
import domain.usecase.PeopleTypeUseCase;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PeopleTypeController {
    @FXML
    private TextField txtNombre;
    @FXML private TableView<PeopleType> tbl;
    @FXML private TableColumn<PeopleType, Long> colId;
    @FXML private TableColumn<PeopleType, String> colNombre;

    private final PeopleTypeUseCase useCase;
    private final ObservableList<PeopleType> data = FXCollections.observableArrayList();
    private PeopleType seleccionado;

    public PeopleTypeController(PeopleTypeUseCase useCase){
        this.useCase = useCase;
    }

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getId()).asObject());
        colNombre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNombre()));
        tbl.setItems(data);
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadForm(newV));
        refrescar();
    }

    private void loadForm(PeopleType t){
        if (t==null){ nuevo(); return; }
        seleccionado = t;
        txtNombre.setText(t.getNombre());
    }

    @FXML
    public void nuevo(){
        seleccionado = null;
        txtNombre.clear();
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    public void guardar(){
        String nombre = txtNombre.getText();
        if (nombre==null || nombre.isBlank()){ show("Nombre requerido"); return; }
        try{
            if (seleccionado==null){
                useCase.crear(nombre);
            } else {
                useCase.actualizar(seleccionado.getId(), nombre);
            }
            refrescar();
            nuevo();
        } catch (RuntimeException ex){
            show(ex.getMessage());
        }
    }

    @FXML
    public void eliminar(){
        if (seleccionado==null){ show("Seleccione un registro"); return; }
        try{
            useCase.eliminar(seleccionado.getId());
            refrescar(); nuevo();
        } catch (RuntimeException ex){
            show(ex.getMessage());
        }
    }

    @FXML
    public void refrescar(){
        data.setAll(useCase.listar());
    }

    private void show(String msg){
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }
}
