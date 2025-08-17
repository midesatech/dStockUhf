
package infrastructure.fx.controller.system;

import app.config.AppBootstrap;
import app.session.UserSession;
import domain.usecase.UserUseCase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {
    @FXML
    private PasswordField txtCurrent;
    @FXML
    private PasswordField txtNext;
    @FXML
    private PasswordField txtConfirm;

    private final UserUseCase users = AppBootstrap.users();

    @FXML
    public void change() {
        Long currentUserId = UserSession.getCurrent().getId();
        if (currentUserId == null) {
            alert("No hay un usuario logueado");
            return;
        }
        String c = txtCurrent.getText();
        String n = txtNext.getText();
        String r = txtConfirm.getText();
        if (n == null || n.isBlank() || !n.equals(r)) {
            alert("La nueva contraseña y su confirmación no coinciden");
            return;
        }
        try {
            users.changePassword(currentUserId, c, n);
            alert("Contraseña actualizada");
            txtCurrent.clear();
            txtNext.clear();
            txtConfirm.clear();
        } catch (Exception ex) {
            alert(ex.getMessage());
        }
    }

    private void alert(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }
}
