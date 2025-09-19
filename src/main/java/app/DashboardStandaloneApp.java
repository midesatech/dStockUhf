package app;

import app.config.ControllerFactory;
import infrastructure.fx.controller.dashboard.DashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardStandaloneApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        infrastructure.persistence.JPAUtil.init(); // inicializa JPA (persistence.xml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/dashboard.fxml"));
        // Si en el futuro necesitas DI:
        // loader.setControllerFactory(new app.config.ControllerFactory());
        Parent root = loader.load();
        stage.setTitle("Dashboard de Ocupación UHF");
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
