
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
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

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
            // 1) Stop background stuff and clear session
            stopReaderDetection();                 // if you run a reader task
            UserSession.clear();

            // 2) Load login view
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/infrastructure/fx/view/login.fxml")
            );
            Parent loginRoot = loader.load();

            // 3) Get the Stage from any node you control (contentArea, root, etc.)
            Stage stage = (Stage) contentArea.getScene().getWindow();

            // 4) Reset size constraints so the login can size itself
            stage.setResizable(true);
            stage.setMinWidth(0);     // ✅ non-negative
            stage.setMinHeight(0);

            // 5) Set a NEW scene AND the intended size (match your MainApp)
            Scene loginScene = new Scene(loginRoot, 400, 300);
            stage.setScene(loginScene);

            // If you attach global stylesheets, re-attach them here as needed:
            // loginScene.getStylesheets().add(getClass().getResource("/app.css").toExternalForm());

            // 6) Let JavaFX compute the proper window size and position
            stage.sizeToScene();
            stage.centerOnScreen();
            stage.setTitle("Inventario - Login");
        } catch (IOException ex) {
            ex.printStackTrace();
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