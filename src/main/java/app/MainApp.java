package app;

import app.config.AppBootstrap;
import app.config.ControllerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // If config file does not exist → show config screen first (no JPA yet)
        if (!app.config.PropertyConfigService.exists()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/infrastructure/fx/view/system/db_config.fxml"));
            // pass controller factory only for consistency (not strictly required here)
            loader.setControllerFactory(new app.config.ControllerFactory());
            javafx.scene.Parent root = loader.load();

            infrastructure.fx.controller.system.DbConfigController ctrl =
                    loader.getController();
            ctrl.setStartupMode(true);         // ↩ important
            ctrl.setStage(stage);              // we will swap to Login after save

            stage.setTitle("Configuración de Base de Datos");
            stage.setScene(new javafx.scene.Scene(root, 520, 360));
            stage.show();
            return; // defer AppBootstrap.init(...) until user saves & tests
        }

        // Normal flow (config already present)
        app.config.AppBootstrap.init(true);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "/infrastructure/fx/view/login.fxml"));
        loader.setControllerFactory(new app.config.ControllerFactory());

        javafx.scene.Parent root = loader.load();
        stage.setTitle("Inventario - Login");
        stage.setScene(new javafx.scene.Scene(root, 400, 300));
        stage.show();
    }


    @Override
    public void stop() {
        AppBootstrap.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}