
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
    private ChoiceBox<ThemeManager.UiTheme> themeChoice;

    @FXML
    public void initialize() {
        txtPass.setOnKeyPressed(e -> checkCapsLock());
        txtPass.setOnKeyReleased(e -> checkCapsLock());
        themeChoice.getItems().setAll(
                ThemeManager.UiTheme.OCEAN,
                ThemeManager.UiTheme.GREEN,
                ThemeManager.UiTheme.DARK,
                ThemeManager.UiTheme.OBSIDIAN
        );
        themeChoice.setValue(ThemeManager.getTheme());
    }

    @FXML
    public void login() {
        try {
            User u = AppBootstrap.auth().authenticate(txtUser.getText(), txtPass.getText());
            UserSession.setCurrent(u);
            ThemeManager.UiTheme sel = themeChoice.getValue();
            if (sel != null) ThemeManager.setTheme(sel);
            // open main window
            Stage stage = (Stage) txtUser.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/main.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.setUsuario(u.getUsername());

            stage.setScene(new Scene(root, 1024, 768));
            stage.setMinWidth(1366);  // evita que se encoja demasiado
            stage.setMinHeight(760);
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
