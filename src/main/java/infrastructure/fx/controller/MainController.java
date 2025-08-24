
package infrastructure.fx.controller;

import app.config.AppBootstrap;
import app.session.UserSession;
import domain.model.User;
import domain.usecase.tag.ReaderUseCase;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MainController {
    @FXML private StackPane contentArea;
    @FXML private Label lblUser;
    @FXML private Label lblCapsStatus;
    @FXML private Label lblStatus;
    @FXML private Label lblLector;
    @FXML private Label lblTagDetection;

    private static MainController instance;
    private ReaderUseCase readerUseCase;

    private static final Logger infLog = LogManager.getLogger("infLogger");
    private static final Logger errLog = LogManager.getLogger("errLogger");


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
        setReaderUseCase(AppBootstrap.readerUseCase());
        loadReaders();
    }

    public void setReaderUseCase(ReaderUseCase readerUseCase) {
        this.readerUseCase = readerUseCase;
        this.readerUseCase.messageProperty().addListener((observable, oldMessage, newMessage) -> {
            lblLector.setText(newMessage);
        });
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

    public void setTagDetectionStatus(String message) {
        lblTagDetection.setText(message);
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

    private void loadReaders() {
        startReaderDetection();
    }

    public void startReaderDetection() {
        Worker.State currentState = this.readerUseCase.getState();
        if (currentState == Worker.State.READY) {
            infLog.info("Starting reader task (first run or after reset)...");
            this.readerUseCase.start();
        } else if (currentState == Worker.State.RUNNING) {
            infLog.info("Reader task is already running.");
        } else {
            infLog.info("Reader task is in state {}. Restarting...", currentState);
            this.readerUseCase.restart();
        }
    }

    public void stopReaderDetection() {
        if (this.readerUseCase!=null && this.readerUseCase.isRunning()) {
            this.readerUseCase.cancel();
        }
    }

}