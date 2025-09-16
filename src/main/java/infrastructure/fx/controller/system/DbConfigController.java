package infrastructure.fx.controller.system;

import app.config.AppBootstrap;
import app.config.ControllerFactory;
import app.config.PropertyConfigService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DbConfigController {

    @FXML private TextField txtHost;
    @FXML private TextField txtPort;
    @FXML private TextField txtUser;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMessage;

    private boolean startupMode = false;
    private Stage stage;

    @FXML
    public void initialize() {
        // Prefill with existing data (if file exists)
        Properties p = PropertyConfigService.load();
        txtHost.setText(p.getProperty(PropertyConfigService.KEY_DB_HOST, "localhost"));
        txtPort.setText(p.getProperty(PropertyConfigService.KEY_DB_PORT, "3306"));
        txtUser.setText(p.getProperty(PropertyConfigService.KEY_DB_USER, "root"));
        txtPassword.setText(p.getProperty(PropertyConfigService.KEY_DB_PASSWORD, ""));
    }

    public void setStartupMode(boolean startupMode) {
        this.startupMode = startupMode;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    public void testConnection() {
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();
        String user = txtUser.getText().trim();
        String pass = txtPassword.getText();

        if (host.isEmpty() || port.isEmpty() || user.isEmpty()) {
            setInfo("Please fill Host, Port and User.", true);
            return;
        }
        String url = "jdbc:mariadb://" + host + ":" + port + "/inventario";
        try {
            // Load driver explicitly (optional in modern JDKs; safe)
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection ignored = DriverManager.getConnection(url, user, pass)) {
                setInfo("✅ Connection OK: " + url, false);
            }
        } catch (Exception e) {
            setInfo("❌ Connection failed: " + e.getMessage(), true);
        }
    }

    @FXML
    public void save() {
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();
        String user = txtUser.getText().trim();
        String pass = txtPassword.getText();

        if (host.isEmpty() || port.isEmpty() || user.isEmpty()) {
            setInfo("Please fill Host, Port and User.", true);
            return;
        }

        // Try connection first
        String url = "jdbc:mariadb://" + host + ":" + port + "/inventario";
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            try (Connection ignored = DriverManager.getConnection(url, user, pass)) {
                // OK → persist
                PropertyConfigService.save(host, port, user, pass);
                setInfo("✅ Saved. Connection OK.", false);

                if (startupMode) {
                    // Now we can init the app and go to Login
                    AppBootstrap.init(true);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(
                            "/infrastructure/fx/view/login.fxml"));
                    loader.setControllerFactory(new ControllerFactory());
                    Parent root = loader.load();
                    stage.setTitle("Inventario - Login");
                    stage.setScene(new Scene(root, 400, 300));
                } else {
                    // In-app edit: just notify
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Configuration saved.", ButtonType.OK);
                    a.showAndWait();
                }
            }
        } catch (Exception e) {
            setInfo("❌ Save cancelled. Connection failed: " + e.getMessage(), true);
        }
    }

    @FXML
    public void cancel() {
        if (startupMode) {
            // No config → cannot continue; exit gracefully
            if (stage != null) stage.close();
        } else {
            // In-app navigation: simply go back (close this window if opened in a new Stage)
            if (stage != null) stage.close();
        }
    }

    private void setInfo(String msg, boolean error) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("success", "error");
        lblMessage.getStyleClass().add(error ? "error" : "success");
    }

}