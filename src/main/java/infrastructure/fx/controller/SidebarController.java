package infrastructure.fx.controller;

import app.config.ControllerFactory;
import app.session.UserSession;
import domain.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

import java.io.IOException;

public class SidebarController {

    @FXML private VBox root;          // contenedor raíz (mantiene el botón)
    @FXML private VBox menuWrapper;   // SOLO esto se colapsa
    @FXML private TreeView<String> menuTree;

    private boolean collapsed = false;
    private static final double EXPANDED_WIDTH = 260;
    private static final double COLLAPSED_WIDTH = 56;

    @FXML
    public void initialize() {
        // Estado inicial expandido
        root.setPrefWidth(EXPANDED_WIDTH);
        menuWrapper.setVisible(true);
        menuWrapper.setManaged(true);

        // CellFactory único: muestra SOLO el graphic (ícono + label) y aplica colores pastel intercalados
        menuTree.setCellFactory(tv -> new TreeCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTreeItem() == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    setText(null); // no mostramos el value textual del TreeItem
                    setGraphic(getTreeItem().getGraphic());

                    // alterna colores pastel por fila (si quieres otros colores cámbialos)
                    int rowIndex = tv.getRow(getTreeItem());
                    if (rowIndex % 2 == 0) {
                        setStyle("-fx-background-color: #fce4ec;"); // pastel rosado
                    } else {
                        setStyle("-fx-background-color: #e3f2fd;"); // pastel azul
                    }
                }
            }
        });

        buildMenu();
    }

    @FXML
    public void toggle(ActionEvent event) {
        collapsed = !collapsed;

        if (collapsed) {
            // Colapsado → solo se muestra el botón
            menuWrapper.setVisible(false);
            menuWrapper.setManaged(false);
            root.setPrefWidth(Region.USE_COMPUTED_SIZE); // ancho solo lo que ocupa el botón
            root.setMinWidth(Region.USE_COMPUTED_SIZE);
        } else {
            // Expandido → sidebar completo
            menuWrapper.setVisible(true);
            menuWrapper.setManaged(true);
            root.setPrefWidth(EXPANDED_WIDTH);
            root.setMinWidth(EXPANDED_WIDTH);
        }
    }

    private void buildMenu() {
        User u = UserSession.getCurrent();
        boolean isAdmin = u != null && "ADMIN".equalsIgnoreCase(u.getUsername());

        TreeItem<String> rootItem = new TreeItem<>();
        rootItem.setExpanded(true);

        // 1. Catálogos (subitems 1.1..1.5)
        TreeItem<String> catRoot = makeMenuCategory("Catálogos", Icons.BOOK);
        if (isAdmin || hasPermission(u, "CATALOG_READ")) {
            catRoot.getChildren().add(makeMenuItem("Categorías", "/infrastructure/fx/view/catalog/categorias.fxml", Icons.CATEGORY));
            catRoot.getChildren().add(makeMenuItem("Ubicaciones", "/infrastructure/fx/view/catalog/ubicaciones.fxml", Icons.MAP));
            catRoot.getChildren().add(makeMenuItem("Sub ubicación", "/infrastructure/fx/view/catalog/sububicacion.fxml", Icons.LINK));
            catRoot.getChildren().add(makeMenuItem("Producto", "/infrastructure/fx/view/catalog/productos.fxml", Icons.BOX));
            catRoot.getChildren().add(makeMenuItem("Empleados", "/infrastructure/fx/view/catalog/empleados.fxml", Icons.USER));
        }
        rootItem.getChildren().add(catRoot);

        // 2. Administración de inventario (2.1)
        TreeItem<String> invRoot = makeMenuCategory("Administración de inventario", Icons.SETTINGS);
        if (isAdmin || hasPermission(u, "INVENTORY_ASSIGN")) {
            invRoot.getChildren().add(makeMenuItem("Ubicación de productos", "/infrastructure/fx/view/catalog/ubicacion_productos.fxml", Icons.LINK));
        }
        rootItem.getChildren().add(invRoot);

        // 3. Administración del sistema (3.1..3.4)
        TreeItem<String> sysRoot = makeMenuCategory("Administración del sistema", Icons.SHIELD);
        if (isAdmin || hasPermission(u, "ROLE_MANAGE")) {
            sysRoot.getChildren().add(makeMenuItem("Roles", "/infrastructure/fx/view/system/roles.fxml", Icons.SHIELD));
        }
        if (isAdmin || hasPermission(u, "USER_MANAGE")) {
            sysRoot.getChildren().add(makeMenuItem("Usuario del sistema", "/infrastructure/fx/view/system/users.fxml", Icons.USER));
        }
        if (isAdmin || hasPermission(u, "ROLE_MANAGE")) {
            sysRoot.getChildren().add(makeMenuItem("Permisos por roles", "/infrastructure/fx/view/system/permissions.fxml", Icons.KEY));
        }
        // 3.4 = Cambio de contraseña → siempre visible
        sysRoot.getChildren().add(makeMenuItem("Cambio de contraseña", "/infrastructure/fx/view/system/change_password.fxml", Icons.LOCK));

        rootItem.getChildren().add(sysRoot);

        menuTree.setRoot(rootItem);
        menuTree.setShowRoot(false);

        // Click para cargar vistas: solo si el graphic tiene userData (fxmlPath)
        menuTree.setOnMouseClicked(e -> {
            TreeItem<String> item = menuTree.getSelectionModel().getSelectedItem();
            if (item != null && item.getGraphic() instanceof HBox) {
                Object ud = ((HBox) item.getGraphic()).getUserData();
                if (ud instanceof String) {
                    loadView((String) ud);
                }
            }
        });
    }

    private TreeItem<String> makeMenuCategory(String label, String svgPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.DARKSLATEGRAY);
        icon.setScaleX(0.6);
        icon.setScaleY(0.6);

        Label text = new Label(label);
        text.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        HBox box = new HBox(8, icon, text);
        box.setAlignment(Pos.CENTER_LEFT);
        // category root typically has no fxml userData (not clickable), so NO setUserData

        // IMPORTANT: use null for value to avoid double text
        return new TreeItem<>(null, box);
    }

    private TreeItem<String> makeMenuItem(String label, String fxmlPath, String svgPath) {
        SVGPath icon = new SVGPath();
        icon.setContent(svgPath);
        icon.setFill(Color.DIMGRAY);
        icon.setScaleX(0.6);
        icon.setScaleY(0.6);

        Label text = new Label(label);
        text.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");

        HBox box = new HBox(8, icon, text);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setUserData(fxmlPath); // aquí sí guardamos el FXML

        return new TreeItem<>(null, box);
    }

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            // ✅ Asignar ControllerFactory para inyectar UseCases
            loader.setControllerFactory(new ControllerFactory());

            Node view = loader.load();
            MainController.getInstance().setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasPermission(User u, String perm) {
        if (u == null) return false;
        if ("ADMIN".equalsIgnoreCase(u.getUsername())) return true;
        return u.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> perm.equalsIgnoreCase(p.getName()));
    }
}