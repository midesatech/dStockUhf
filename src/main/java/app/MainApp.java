package app;

import app.config.AppBootstrap;
import app.config.ControllerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;

import java.util.Optional;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // 1) First-run flow (no properties yet) → open DB Config screen
        if (!app.config.PropertyConfigService.exists()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/infrastructure/fx/view/system/db_config.fxml"));
            loader.setControllerFactory(new app.config.ControllerFactory());
            Parent root = loader.load();

            infrastructure.fx.controller.system.DbConfigController ctrl = loader.getController();
            ctrl.setStartupMode(true);
            ctrl.setStage(stage);

            stage.setTitle("Configuración de Base de Datos");
            stage.setScene(new Scene(root, 520, 360));
            stage.show();
            return;
        }

        // 2) Normal flow → try to init JPA, but handle DB down with a nice dialog
        try {
            app.config.AppBootstrap.init(true);
            // go to Login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/login.fxml"));
            loader.setControllerFactory(new app.config.ControllerFactory());
            Parent root = loader.load();
            stage.setTitle("Inventario - Login");
            stage.setScene(new Scene(root, 400, 300));
            stage.show();
        } catch (Exception ex) {
            // Show custom dialog with Retry / Configure / Exit
            showDbErrorDialog(stage, ex);
        }
    }

    /**
     * Shows a Matrix-style dialog when DB init fails. Lets user Retry, open DB Config, or Exit.
     */
    private void showDbErrorDialog(Stage stage, Exception ex) {
        // Pull current effective host/port/user for context
        String host = app.config.PropertyConfigService.get(app.config.PropertyConfigService.KEY_DB_HOST, "localhost");
        String port = app.config.PropertyConfigService.get(app.config.PropertyConfigService.KEY_DB_PORT, "3306");
        String user = app.config.PropertyConfigService.get(app.config.PropertyConfigService.KEY_DB_USER, "root");
        String url  = "jdbc:mariadb://" + host + ":" + port + "/inventario";

        String rootMsg = rootCauseMessage(ex);

        ButtonType btnRetry = new ButtonType("Retry", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnConfig = new ButtonType("Open DB Config", ButtonBar.ButtonData.OTHER);
        ButtonType btnExit = new ButtonType("Exit", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.ERROR,
                "Cannot connect to database.\n\n" +
                        "URL: " + url + "\n" +
                        "User: " + user + "\n\n" +
                        "Cause: " + rootMsg + "\n\n" +
                        "What would you like to do?",
                btnRetry, btnConfig, btnExit);
        alert.setTitle("Database Connection Error");
        alert.setHeaderText("Database is unreachable");

        // Matrix pastel styling
        DialogPane pane = alert.getDialogPane();
        pane.getStylesheets().add(getClass().getResource("/infrastructure/fx/styles/db_error.css").toExternalForm());
        pane.getStyleClass().add("db-error");

        // Show and handle choice
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty()) {
            javafx.application.Platform.exit();
            return;
        }

        if (result.get() == btnRetry) {
            try {
                app.config.AppBootstrap.init(true);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/login.fxml"));
                loader.setControllerFactory(new app.config.ControllerFactory());
                Parent root = loader.load();
                stage.setTitle("Inventario - Login");
                stage.setScene(new Scene(root, 400, 300));
                stage.show();
            } catch (Exception retryEx) {
                // Show again if still failing
                showDbErrorDialog(stage, retryEx);
            }
        } else if (result.get() == btnConfig) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/system/db_config.fxml"));
                loader.setControllerFactory(new app.config.ControllerFactory());
                Parent root = loader.load();

                infrastructure.fx.controller.system.DbConfigController ctrl = loader.getController();
                ctrl.setStartupMode(true); // after save → init & go to Login
                ctrl.setStage(stage);

                stage.setTitle("Configuración de Base de Datos");
                stage.setScene(new Scene(root, 520, 360));
                stage.show();
            } catch (Exception loadEx) {
                // If even config cannot load, exit safely
                showFatal(loadEx);
            }
        } else {
            javafx.application.Platform.exit();
        }
    }

    private void showFatal(Exception ex) {
        Alert a = new Alert(Alert.AlertType.ERROR, "Fatal: " + rootCauseMessage(ex));
        a.setTitle("Fatal Error");
        a.setHeaderText("Application cannot continue");
        a.showAndWait();
        javafx.application.Platform.exit();
    }

    private static String rootCauseMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        String msg = cur.getMessage();
        return (msg == null || msg.isBlank()) ? cur.getClass().getSimpleName() : msg;
    }

    @Override
    public void stop() {
        AppBootstrap.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}