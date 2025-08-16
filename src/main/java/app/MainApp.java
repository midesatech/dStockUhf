package app;

import app.config.AppBootstrap;
import infrastructure.fx.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override public void start(Stage stage) throws Exception {
        // initialize with JPA=false by default (in-memory). Change to true to use MariaDB.
        AppBootstrap.init(true);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/infrastructure/fx/view/login.fxml"));
        Parent root = loader.load();
        stage.setTitle("Inventario - Login");
        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }
    @Override public void stop(){ AppBootstrap.shutdown(); }
    public static void main(String[] args){ launch(args); }
}