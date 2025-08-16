
package infrastructure.fx.controller.system;
import domain.model.Role; import domain.model.Permission; import javafx.collections.FXCollections; import javafx.collections.ObservableList; import javafx.fxml.FXML; import javafx.scene.control.*;
public class RolesController {
    @FXML private TableView<Role> tbl; @FXML private TextField txtName; @FXML private ListView<Permission> lstPermissions;
    private final ObservableList<Role> data = FXCollections.observableArrayList(); private final ObservableList<Permission> perms = FXCollections.observableArrayList();
    @FXML public void initialize(){ if(tbl!=null) tbl.setItems(data); if(lstPermissions!=null){ lstPermissions.setItems(perms); lstPermissions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); } perms.setAll(new Permission(1L,"READ"), new Permission(2L,"WRITE"), new Permission(3L,"ADMIN")); }
    @FXML public void newRole(){ txtName.clear(); tbl.getSelectionModel().clearSelection(); } @FXML public void saveRole(){ Role r = tbl.getSelectionModel().getSelectedItem(); if(r==null) r = new Role(null, txtName.getText()); r.setName(txtName.getText()); if(!data.contains(r)) data.add(r); } @FXML public void deleteRole(){ Role r = tbl.getSelectionModel().getSelectedItem(); if(r!=null) data.remove(r); }
}
