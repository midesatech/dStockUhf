
package infrastructure.fx.controller.catalog;
import app.config.AppBootstrap;
import domain.model.Empleado;
import domain.usecase.EmpleadoUseCase;
import javafx.collections.FXCollections; import javafx.collections.ObservableList; import javafx.fxml.FXML; import javafx.scene.control.*;
public class EmpleadosController {
    @FXML private TableView<Empleado> tbl; @FXML private TextField txtCodigo; @FXML private TextField txtNombre;
    private final ObservableList<Empleado> data = FXCollections.observableArrayList();
    private EmpleadoUseCase useCase;
    @FXML public void initialize(){ useCase = AppBootstrap.isJpaMode()? new EmpleadoUseCase(new infrastructure.adapter.database.jpa.EmpleadoRepositoryAdapter(infrastructure.persistence.JPAUtil.getEmf())): null; if(tbl!=null) tbl.setItems(data); refresh(); }
    public void refresh(){ data.clear(); if(useCase!=null) data.addAll(useCase.listar()); }
    @FXML public void nuevo(){ txtCodigo.clear(); txtNombre.clear(); tbl.getSelectionModel().clearSelection(); }
    @FXML public void guardar(){ try{ if(useCase==null) throw new IllegalStateException("Use JPA mode"); useCase.crear(txtCodigo.getText(), txtNombre.getText()); refresh(); new Alert(Alert.AlertType.INFORMATION,"Guardado").showAndWait(); }catch(Exception ex){ new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); } }
    @FXML public void eliminar(){ Empleado s = tbl.getSelectionModel().getSelectedItem(); if(s==null){ new Alert(Alert.AlertType.INFORMATION,"Seleccione"); return;} try{ useCase.eliminar(s.getId()); refresh(); new Alert(Alert.AlertType.INFORMATION,"Eliminado").showAndWait(); }catch(Exception ex){ new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); } }
}
