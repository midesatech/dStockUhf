
package infrastructure.fx.controller.system;

import domain.model.User;
import domain.usecase.UserUseCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class UserController {
    @FXML
    private TableView<User> tbl;
    @FXML
    private TableColumn<User, Long> colId;
    @FXML
    private TableColumn<User, String> colUser;
    @FXML
    private TableColumn<User, Boolean> colSystem;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private CheckBox chkSystemUser;
    private final UserUseCase userUseCase;
    private final ObservableList<User> data = FXCollections.observableArrayList();

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colSystem.setCellValueFactory(new PropertyValueFactory<>("systemUser"));
        tbl.setItems(data);
        refresh();
    }

    @FXML
    public void newUser() {
        txtUsername.clear();
        txtPassword.clear();
        chkSystemUser.setSelected(false);
        tbl.getSelectionModel().clearSelection();
    }

    @FXML
    public void saveUser() {
        try {
            String username = txtUsername.getText();
            String rawPassword = txtPassword.getText();

            User user = userUseCase.findByUsername(username).orElse(new User());
            user.setUsername(username);
            user.setSystemUser(chkSystemUser.isSelected());

            // ahora delegamos toda la lógica a UserUseCase
            userUseCase.save(user, rawPassword);

            refresh();
            show("Guardado");
            txtUsername.clear();
            txtPassword.clear();
        } catch (Exception ex) {
            show(ex.getMessage());
        }
    }

    @FXML
    public void deleteUser() {
        User sel = tbl.getSelectionModel().getSelectedItem();
        if (sel == null) {
            show("Seleccione un usuario");
            return;
        }
        try {
            userUseCase.delete(sel.getId());
            refresh();
        } catch (Exception ex) {
            show(ex.getMessage());
        }
    }

    @FXML
    public void refresh() {
        data.setAll(userUseCase.listAll());
    }

    private void show(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }
}
