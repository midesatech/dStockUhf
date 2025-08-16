
package infrastructure.fx.controller.system;

import domain.model.Role;
import domain.model.Permission;
import domain.usecase.PermissionUseCase;
import domain.usecase.RoleUseCase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.Set;

public class RolesController {
    @FXML
    private TableView<Role> tbl;
    @FXML
    private TableColumn<Role, Long> colId;
    @FXML
    private TableColumn<Role, String> colName;
    @FXML
    private TableColumn<Role, String> colPerms;
    @FXML
    private TextField txtName;
    @FXML
    private ListView<Permission> lstPermissions;
    private final ObservableList<Role> data = FXCollections.observableArrayList();
    private final ObservableList<Permission> perms = FXCollections.observableArrayList();

    private final RoleUseCase roleUseCase;
    private final PermissionUseCase permissionUseCase;

    public RolesController(RoleUseCase roleUseCase, PermissionUseCase permissionUseCase) {
        this.roleUseCase = roleUseCase;
        this.permissionUseCase = permissionUseCase;
    }

    @FXML
    public void initialize() {
        if (tbl != null) {
            tbl.setItems(data);
            colId.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getId()));
            colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
            colPerms.setCellValueFactory(c ->
                    new SimpleStringProperty(
                            c.getValue().getPermissions().stream()
                                    .map(Permission::getName)
                                    .sorted()
                                    .reduce((a, b) -> a + ", " + b)
                                    .orElse("")
                    )
            );
        }
        perms.setAll(permissionUseCase.findAll());

        if (lstPermissions != null) {
            lstPermissions.setItems(perms);
            lstPermissions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        }

        data.setAll(roleUseCase.listAll());

        // 🔹 Evento al seleccionar fila en tabla → llenar campo txtName
        tbl.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtName.setText(sel.getName());
                lstPermissions.getSelectionModel().clearSelection();
                for (Permission p : sel.getPermissions()) {
                    lstPermissions.getSelectionModel().select(p);
                }
            } else {
                txtName.clear();
                lstPermissions.getSelectionModel().clearSelection();
            }
        });
    }

    @FXML
    public void newRole() {
        txtName.clear();
        tbl.getSelectionModel().clearSelection();
        lstPermissions.getSelectionModel().clearSelection();
    }

    @FXML
    public void saveRole() {
        String name = txtName.getText();
        if (name == null || name.isBlank()) return;

        Role selected = tbl.getSelectionModel().getSelectedItem();
        ObservableList<Permission> selectedPerms = lstPermissions.getSelectionModel().getSelectedItems();

        if (selected != null) {
            // Actualizar rol existente
            selected.setName(name);
            selected.setPermissions(Set.copyOf(selectedPerms));
            roleUseCase.create(selected); // persistir cambios
            tbl.refresh();
        } else {
            // Crear nuevo rol
            Role r = new Role(name);
            r.setPermissions(Set.copyOf(selectedPerms));
            Role saved = roleUseCase.create(r);
            data.add(saved);
            tbl.getSelectionModel().select(saved);
        }
    }

    @FXML
    public void deleteRole() {
        Role selected = tbl.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar eliminación");
        alert.setHeaderText("¿Eliminar rol?");
        alert.setContentText("¿Deseas eliminar el rol: " + selected.getName() + "?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                roleUseCase.delete(selected.getId());
                data.remove(selected);
                txtName.clear();
                lstPermissions.getSelectionModel().clearSelection();
            }
        });
    }

    @FXML
    public void refreshRoles() {
        data.setAll(roleUseCase.listAll());
        tbl.getSelectionModel().clearSelection();
        txtName.clear();
        lstPermissions.getSelectionModel().clearSelection();
    }

}
