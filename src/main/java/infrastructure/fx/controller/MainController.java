
package infrastructure.fx.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;

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
}