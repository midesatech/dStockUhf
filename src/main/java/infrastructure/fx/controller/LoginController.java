
package infrastructure.fx.controller;

import app.config.AppBootstrap;
import app.session.UserSession;
import domain.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPass;
    @FXML
    private Label lblMsg;
    @FXML
    private Label lblCapsLock;

    @FXML
    public void initialize() {
        txtPass.setOnKeyPressed(e -> checkCapsLock());
        txtPass.setOnKeyReleased(e -> checkCapsLock());
    }

    @FXML
    public void login() {
        try {
            User u = AppBootstrap.auth().authenticate(txtUser.getText(), txtPass.getText());
            UserSession.setCurrent(u);
            // open main window
            Stage stage = (Stage) txtUser.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/main.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUsuario(u.getUsername());

            stage.setScene(new Scene(root, 1000, 650));
            stage.setTitle("Inventario - " + u.getUsername());
        } catch (Exception ex) {
            lblMsg.setText(ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    private void checkCapsLock() {
        try {
            boolean caps = java.awt.Toolkit.getDefaultToolkit()
                    .getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
            lblCapsLock.setVisible(caps);
        } catch (UnsupportedOperationException ex) {
            // Algunos entornos Linux/Wayland pueden no soportarlo
            lblCapsLock.setVisible(false);
        }
    }
}
