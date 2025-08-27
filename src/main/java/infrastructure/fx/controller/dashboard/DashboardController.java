package infrastructure.fx.controller.dashboard;

import infrastructure.fx.viewmodel.LocationPresence;
import infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

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

    private final List<VBox> cardPool = new ArrayList<>();
    private final Timer timer = new Timer("dashboard-refresh", true);
    private int timeoutSeconds = 86400; // configurable

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
    }
}
