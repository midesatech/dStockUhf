
package infrastructure.fx.controller.system;
import app.config.AppBootstrap;
import domain.gateway.UserRepositoryPort;
import domain.model.User;
import javafx.collections.FXCollections; import javafx.collections.ObservableList; import javafx.fxml.FXML; import javafx.scene.control.*;
public class UsersController {
    @FXML private TableView<User> tbl; @FXML private TableColumn<User, Long> colId; @FXML private TableColumn<User, String> colUser; @FXML private TableColumn<User, Boolean> colSystem;
    @FXML private TextField txtUsername; @FXML private PasswordField txtPassword; @FXML private CheckBox chkSystemUser;
    private final UserRepositoryPort users = AppBootstrap.users();
    private final ObservableList<User> data = FXCollections.observableArrayList();
    @FXML public void initialize(){ if(tbl!=null) tbl.setItems(data); refresh(); }
    @FXML public void newUser(){ txtUsername.clear(); txtPassword.clear(); chkSystemUser.setSelected(false); tbl.getSelectionModel().clearSelection(); }
    @FXML public void saveUser(){ String u = txtUsername.getText(); if(u==null || u.isBlank()){ show("Usuario requerido"); return; } User found = users.findByUsername(u).orElse(new User()); found.setUsername(u); if(!txtPassword.getText().isBlank()){ found.setPasswordHash(AppBootstrap.encoder().encode(txtPassword.getText())); } else if(found.getPasswordHash()==null){ show("Contraseña requerida"); return; } found.setSystemUser(chkSystemUser.isSelected()); users.save(found); refresh(); show("Guardado"); }
    @FXML public void deleteUser(){ User sel = tbl.getSelectionModel().getSelectedItem(); if(sel==null){ show("Seleccione un usuario"); return; } try{ users.deleteById(sel.getId()); refresh(); }catch(Exception ex){ show(ex.getMessage()); } }
    private void refresh(){ data.clear(); for(long i=1;i<=50;i++){ users.findById(i).ifPresent(data::add); } }
    private void show(String m){ new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
}
