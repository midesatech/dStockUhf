package infrastructure.fx.controller.dashboard;

import infrastructure.fx.viewmodel.LocationPresence;
import infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

import java.sql.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/** Dashboard pastel de ocupación por ubicación.
 *  Approach A: calcula presencia directo desde detecciones_tags (sin escrituras).
 */
public class DashboardController {

    @FXML private FlowPane tiles;
    @FXML private Label subtitle;
    @FXML private ScrollPane scroller;

    @FXML private TableView<OccupantRow> detailsTable;
    @FXML private TableColumn<OccupantRow, String> colTipo;
    @FXML private TableColumn<OccupantRow, String> colEpc;
    @FXML private TableColumn<OccupantRow, String> colNombre;
    @FXML private TableColumn<OccupantRow, String> colUltimo;

    private final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<VBox> cardPool = new ArrayList<>();
    private final Timer timer = new Timer("dashboard-refresh", true);
    private int timeoutSeconds = 86400; // configurable
    private javafx.scene.Node selectedTile;

    @FXML
    public void initialize() {
        String env = System.getenv("PRESENCE_TIMEOUT_SECONDS");
        if (env != null && !env.isEmpty()) {
            try { timeoutSeconds = Integer.parseInt(env); } catch (NumberFormatException ignored) {}
        }
        subtitle.setText("Últimas lecturas en " + timeoutSeconds + "s (detecciones_tags)");
        refreshNow();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override public void run() { Platform.runLater(() -> refreshNow()); }
        }, 5000, 5000);
        setupDetailsTable();
    }

    private void refreshNow() {
        List<LocationPresence> data = fetchPresenceByLocation(timeoutSeconds);
        ensureCards(data.size());
        for (int i = 0; i < data.size(); i++) {
            LocationPresence lp = data.get(i);
            VBox card = (VBox) tiles.getChildren().get(i);
            updateCard(card, lp, i);
        }
        // Remove extra if less
        while (tiles.getChildren().size() > data.size()) {
            int last = tiles.getChildren().size() - 1;
            tiles.getChildren().remove(last);
            cardPool.remove(last);
        }
    }

    @SuppressWarnings("unchecked")
    private List<LocationPresence> fetchPresenceByLocation(int timeoutSec) {
        List<LocationPresence> list = new ArrayList<>();
        EntityManager em = null;
        try {
            em = JPAUtil.getEmf().createEntityManager();
            String sql = """
                       SELECT u.id AS location_id, u.nombre AS location_name,
                       SUM(CASE WHEN e.id IS NOT NULL  THEN 1 ELSE 0 END) AS employees,
                       SUM(CASE WHEN eq.id IS NOT NULL THEN 1 ELSE 0 END) AS equipment
                FROM detecciones_tags dt
                JOIN (
                  SELECT epc, MAX(created_at) AS last_ts
                  FROM detecciones_tags
                  GROUP BY epc
                ) x ON x.epc = dt.epc AND x.last_ts = dt.created_at
                JOIN ubicaciones u ON u.id = dt.ubicacion_id
                LEFT JOIN tags_uhf t ON t.epc = dt.epc
                LEFT JOIN empleados e ON e.tag_id = t.id
                LEFT JOIN equipment eq ON eq.tag_id = t.id
                WHERE dt.created_at >= ?1
                GROUP BY u.id, u.nombre
                ORDER BY u.nombre ASC
            """;
            Query q = em.createNativeQuery(sql);
            LocalDateTime since = LocalDateTime.now().minusSeconds(timeoutSec);
                q.setParameter(1, since);
            List<Object[]> rows = q.getResultList();
            for (Object[] r : rows) {
                Long id = ((Number) r[0]).longValue();
                String name = (String) r[1];
                int emp = ((Number) r[2]).intValue();
                int eqp = ((Number) r[3]).intValue();
                list.add(new LocationPresence(id, name, emp, eqp));
            }
        } catch (Exception ex) {
            // fallback: show empty; subtitle can show error if desired
        } finally {
            if (em != null) em.close();
        }
        return list;
    }

    private void ensureCards(int n) {
        while (tiles.getChildren().size() < n) {
            VBox card = makeCard();
            tiles.getChildren().add(card);
            cardPool.add(card);
        }
    }

    private VBox makeCard() {
        VBox box = new VBox();
        box.getStyleClass().addAll("location-card", "bg-0");
        box.setPickOnBounds(true);
        box.setStyle("-fx-cursor: hand;");
        Label title = new Label("Ubicación");
        title.getStyleClass().add("card-title");
        Label emp = new Label("Personas: 0");
        emp.getStyleClass().add("card-metric");
        Label eq  = new Label("Equipos: 0");
        eq.getStyleClass().add("card-metric");
        box.getChildren().addAll(title, emp, eq);
        return box;
    }

    private void updateCard(VBox card, LocationPresence lp, int index) {
        // set pastel class
        String cls = "bg-" + (index % 6);
        card.getStyleClass().removeIf(s -> s.startsWith("bg-"));
        card.getStyleClass().add(cls);
        // update labels
        ((Label) card.getChildren().get(0)).setText(lp.getLocationName());
        ((Label) card.getChildren().get(1)).setText("Personas: " + lp.getEmployees());
        ((Label) card.getChildren().get(2)).setText("Equipos: " + lp.getEquipment());
        attachClickHandler(card, lp.getLocationId(), lp.getLocationName());
    }

    private void attachClickHandler(javafx.scene.Node tileNode, long ubicacionId, String ubicacionNombre) {
        tileNode.setOnMouseClicked(e -> onUbicacionSelected(tileNode, ubicacionId, ubicacionNombre));
    }


    private void onUbicacionSelected(javafx.scene.Node tileNode, long ubicacionId, String ubicacionNombre) {
        subtitle.setText("Ubicación: " + ubicacionNombre);
        List<OccupantRow> data = fetchOccupantsByUbicacion(ubicacionId);
        detailsTable.setItems(FXCollections.observableArrayList(data));
        // Quitar selección previa (si había)
        if (selectedTile != null) {
            selectedTile.getStyleClass().remove("selected");
            selectedTile.setScaleX(1.0);
            selectedTile.setScaleY(1.0);
        }
        // Marcar la tarjeta actual
        tileNode.getStyleClass().add("selected");
        selectedTile = tileNode;
        ScaleTransition st = new ScaleTransition(Duration.millis(120), tileNode);
        st.setToX(1.03);
        st.setToY(1.03);
        st.play();
        selectedTile = tileNode;
    }


    private void setupDetailsTable() {
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colEpc.setCellValueFactory(new PropertyValueFactory<>("epc"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colUltimo.setCellValueFactory(new PropertyValueFactory<>("ultimo"));


        detailsTable.setRowFactory(tv -> new TableRow<>() {
            @Override protected void updateItem(OccupantRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-employee", "row-equipment");
                if (empty || item == null) return;
                String t = item.getTipo() == null ? "" : item.getTipo().toUpperCase();
                if (t.contains("EMP")) {
                    getStyleClass().add("row-employee");
                } else {
                    getStyleClass().add("row-equipment");
                }
            }
        });
    }

    private List<OccupantRow> fetchOccupantsByUbicacion(long ubicacionId) {
        List<OccupantRow> rows = new ArrayList<>();

        String url  = "jdbc:mariadb://localhost:3306/inventario";
        String user = "root";
        String pass = "";

        String sql =
                "WITH ld AS ( " +
                        "  SELECT dt.epc, MAX(dt.created_at) AS last_seen " +
                        "  FROM detecciones_tags dt " +
                        "  WHERE dt.ubicacion_id = ? " +
                        "  GROUP BY dt.epc " +
                        ") " +
                        "SELECT " +
                        "  CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'EQUIPMENT' END AS tipo, " +
                        "  tu.epc AS epc, " +
                        "  TRIM(CASE WHEN e.id IS NOT NULL " +
                        "            THEN CONCAT(COALESCE(e.full_name,''), ' ', COALESCE(e.last_name,'')) " +
                        "            ELSE COALESCE(q.nombre,'') END) AS nombre, " +
                        "  ld.last_seen AS last_seen " +
                        "FROM ld " +
                        "JOIN tags_uhf tu ON tu.epc = ld.epc " +
                        "LEFT JOIN empleados e ON e.tag_id = tu.id " +
                        "LEFT JOIN equipment q ON q.tag_id = tu.id " +
                        "ORDER BY ld.last_seen DESC";

        try (Connection cn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, ubicacionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo   = rs.getString("tipo");
                    String epc    = rs.getString("epc");
                    String nombre = rs.getString("nombre");
                    Timestamp ts  = rs.getTimestamp("last_seen");
                    String last   = ts == null ? "" : ts.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDateTime()
                            .format(TS_FMT);

                    // Fallbacks por si viniera vacío:
                    if (nombre == null || nombre.isBlank()) {
                        if (tipo != null && tipo.toUpperCase().contains("EMP")) {
                            nombre = "(Empleado sin nombre)";
                        } else {
                            nombre = "Equipo (" + epc + ")";
                        }
                    }

                    rows.add(new OccupantRow(tipo, epc, nombre, last));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return rows;
    }
}
