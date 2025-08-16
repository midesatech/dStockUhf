
package infrastructure.fx.controller.catalog;
import app.config.AppBootstrap;
import domain.model.Categoria; import domain.model.Ubicacion; import domain.model.Empleado; import domain.model.Producto;
import domain.usecase.CategoriaUseCase; import domain.usecase.UbicacionUseCase; import domain.usecase.EmpleadoUseCase; import domain.usecase.ProductoUseCase;
import javafx.collections.FXCollections; import javafx.collections.ObservableList; import javafx.fxml.FXML; import javafx.scene.control.*;
public class ProductosController {
    @FXML private TableView<Producto> tbl; @FXML private TextField txtSku; @FXML private TextField txtNombre;
    @FXML private ComboBox<Categoria> cmbCategoria; @FXML private ComboBox<Ubicacion> cmbUbicacion; @FXML private ComboBox<Empleado> cmbEmpleado;
    private final ObservableList<Producto> data = FXCollections.observableArrayList();
    private final ObservableList<Categoria> cats = FXCollections.observableArrayList();
    private final ObservableList<Ubicacion> ubic = FXCollections.observableArrayList();
    private final ObservableList<Empleado> emps = FXCollections.observableArrayList();
    private ProductoUseCase useProduct; private CategoriaUseCase useCat; private UbicacionUseCase useUb; private EmpleadoUseCase useEmp;

    @FXML public void initialize(){
        if(!AppBootstrap.isJpaMode()){ return; }
        useProduct = new ProductoUseCase(new infrastructure.adapter.database.jpa.ProductoRepositoryAdapter(infrastructure.persistence.JPAUtil.getEmf()));
        useCat = AppBootstrap.categoriaUseCase();
        useUb = new UbicacionUseCase(new infrastructure.adapter.database.jpa.UbicacionRepositoryAdapter(infrastructure.persistence.JPAUtil.getEmf()));
        useEmp = new EmpleadoUseCase(new infrastructure.adapter.database.jpa.EmpleadoRepositoryAdapter(infrastructure.persistence.JPAUtil.getEmf()));
        cmbCategoria.setItems(cats); cmbUbicacion.setItems(ubic); cmbEmpleado.setItems(emps);
        if(tbl!=null) tbl.setItems(data);
        refreshAll();
    }

    private void refreshAll(){
        data.clear(); cats.clear(); ubic.clear(); emps.clear();
        data.addAll(useProduct.listar()); cats.addAll(useCat.listar()); ubic.addAll(useUb.listar()); emps.addAll(useEmp.listar());
    }

    @FXML public void nuevo(){ txtSku.clear(); txtNombre.clear(); cmbCategoria.getSelectionModel().clearSelection(); cmbUbicacion.getSelectionModel().clearSelection(); cmbEmpleado.getSelectionModel().clearSelection(); tbl.getSelectionModel().clearSelection(); }
    @FXML public void guardar(){
        try{
            if(useProduct==null) throw new IllegalStateException("Use JPA mode");
            Producto p = new Producto(); p.setSku(txtSku.getText()); p.setNombre(txtNombre.getText()); p.setCategoria(cmbCategoria.getValue()); p.setUbicacion(cmbUbicacion.getValue()); p.setResponsable(cmbEmpleado.getValue());
            useProduct.crear(p); refreshAll(); new Alert(Alert.AlertType.INFORMATION, "Guardado").showAndWait();
        }catch(Exception ex){ new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); }
    }
    @FXML public void eliminar(){ Producto sel = tbl.getSelectionModel().getSelectedItem(); if(sel==null){ new Alert(Alert.AlertType.INFORMATION,"Seleccione").showAndWait(); return; } try{ useProduct.eliminar(sel.getId()); refreshAll(); new Alert(Alert.AlertType.INFORMATION,"Eliminado").showAndWait(); }catch(Exception ex){ new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait(); } }
}
