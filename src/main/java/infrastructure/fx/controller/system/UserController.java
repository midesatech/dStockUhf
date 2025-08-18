
package infrastructure.fx.controller.system;

import domain.model.Role;
import domain.model.User;
import domain.usecase.RoleUseCase;
import domain.usecase.UserUseCase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Set;

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
    @FXML
    private ListView<Role> lstRoles;
    private final UserUseCase userUseCase;
    private final ObservableList<User> data = FXCollections.observableArrayList();
    private final ObservableList<Role> allRoles = FXCollections.observableArrayList();
    private final RoleUseCase roleUseCase;

    public UserController(UserUseCase userUseCase, RoleUseCase roleUseCase) {
        this.userUseCase = userUseCase;
        this.roleUseCase = roleUseCase;
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colSystem.setCellValueFactory(new PropertyValueFactory<>("systemUser"));
        tbl.setItems(data);

        // Cargar roles disponibles
        allRoles.setAll(roleUseCase.listAll());
        lstRoles.setItems(allRoles);
        lstRoles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Evento al seleccionar usuario en tabla → llenar formulario
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtUsername.setText(sel.getUsername());
                chkSystemUser.setSelected(sel.isSystemUser());
                txtPassword.clear();

                lstRoles.getSelectionModel().clearSelection();
                for (Role r : sel.getRoles()) {
                    lstRoles.getSelectionModel().select(r);
                }
            } else {
                newUser();
            }
        });

        refresh();
    }

    @FXML
    public void newUser() {
        txtUsername.clear();
        txtPassword.clear();
        chkSystemUser.setSelected(false);
        lstRoles.getSelectionModel().clearSelection();
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
            user.setRoles(Set.copyOf(lstRoles.getSelectionModel().getSelectedItems()));

            userUseCase.save(user, rawPassword);

            refresh();
            show("Guardado");
            newUser();
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
