
package infrastructure.fx.controller.system;
import domain.model.Permission; import javafx.collections.FXCollections; import javafx.collections.ObservableList; import javafx.fxml.FXML; import javafx.scene.control.*;
public class PermissionsController {
    @FXML private TableView<Permission> tbl; @FXML private TextField txtName; private final ObservableList<Permission> data = FXCollections.observableArrayList();
    @FXML public void initialize(){ if(tbl!=null) tbl.setItems(data); } @FXML public void newPermission(){ txtName.clear(); tbl.getSelectionModel().clearSelection(); } @FXML public void savePermission(){ Permission p = tbl.getSelectionModel().getSelectedItem(); if(p==null) p = new Permission(null, txtName.getText()); p.setName(txtName.getText()); if(!data.contains(p)) data.add(p); } @FXML public void deletePermission(){ Permission p = tbl.getSelectionModel().getSelectedItem(); if(p!=null) data.remove(p); }
}
