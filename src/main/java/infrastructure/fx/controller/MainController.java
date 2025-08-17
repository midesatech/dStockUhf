
package infrastructure.fx.controller;

import app.session.UserSession;
import domain.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Label lblUser;
    @FXML private Label lblCapsStatus;
    @FXML private Label lblStatus;

    private static MainController instance;

    @FXML
    public void initialize() {
        // Revisa CapsLock cada 500ms
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.millis(500), e -> checkCapsLock()));
        timeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        timeline.play();
        User current = UserSession.getCurrent();
        if (current != null) {
            lblUser.setText("Usuario: " + current.getUsername());
        } else {
            lblUser.setText("Usuario: [no logueado]");
        }
    }

    public MainController() {
        instance = this;
    }

    public static MainController getInstance() {
        return instance;
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    private void checkCapsLock() {
        try {
            boolean caps = java.awt.Toolkit.getDefaultToolkit()
                    .getLockingKeyState(java.awt.event.KeyEvent.VK_CAPS_LOCK);
            lblCapsStatus.setText("CapsLock: " + (caps ? "ON" : "OFF"));
        } catch (UnsupportedOperationException ex) {
            lblCapsStatus.setText("CapsLock: N/A");
        }
    }

    public void setUsuario(String username) {
        lblUser.setText("Usuario: " + username);
    }

    // ✅ método para actualizar mensajes de estado
    public void setStatus(String message) {
        lblStatus.setText(message);
    }

    @FXML
    public void logout() {
        try {
            // 1. Limpiar sesión
            UserSession.clear();

            // 2. Volver al login
            Stage stage = (Stage) lblUser.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/login.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Inventario - Login");
        } catch (Exception e) {
            e.printStackTrace();
            setStatus("Error al cerrar sesión: " + e.getMessage());
        }
    }
}