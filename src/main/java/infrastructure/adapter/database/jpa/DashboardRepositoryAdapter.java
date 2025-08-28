package infrastructure.adapter.database.jpa;

import domain.gateway.DashboardRepository;
import domain.model.LocationPresence;
import domain.model.Occupant;
import infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;


import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class DashboardRepositoryAdapter implements DashboardRepository {
    private final EntityManagerFactory emf;

    public DashboardRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LocationPresence> fetchPresenceByLocationSince(LocalDateTime since) {
        List<LocationPresence> list = new ArrayList<>();
        EntityManager em = emf.createEntityManager();
        try {
            String sql = """
                    SELECT u.id AS location_id, u.nombre AS location_name,
                    SUM(CASE WHEN e.id IS NOT NULL THEN 1 ELSE 0 END) AS employees,
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
            q.setParameter(1, since);
            List<Object[]> rows = q.getResultList();
            for (Object[] r : rows) {
                Long id = ((Number) r[0]).longValue();
                String name = (String) r[1];
                int emp = ((Number) r[2]).intValue();
                int eqp = ((Number) r[3]).intValue();
                list.add(new LocationPresence(id, name, emp, eqp));
            }
        } finally {
            if (em != null) em.close();
        }
        return list;
    }


    @Override
    public List<Occupant> fetchOccupantsByUbicacion(long ubicacionId) {
        List<Occupant> rows = new ArrayList<>();
        EntityManager em = emf.createEntityManager();
        try {

            String sql = """
                    WITH ld AS (
                    SELECT dt.epc, MAX(dt.created_at) AS last_seen
                    FROM detecciones_tags dt
                    WHERE dt.ubicacion_id = ?1
                    GROUP BY dt.epc
                    )
                    SELECT CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'EQUIPMENT' END AS tipo,
                    tu.epc AS epc,
                    TRIM(CASE WHEN e.id IS NOT NULL
                    THEN CONCAT(COALESCE(e.full_name,''), ' ', COALESCE(e.last_name,''))
                    ELSE COALESCE(q.nombre,'') END) AS nombre,
                    ld.last_seen AS last_seen
                    FROM ld
                    JOIN tags_uhf tu ON tu.epc = ld.epc
                    LEFT JOIN empleados e ON e.tag_id = tu.id
                    LEFT JOIN equipment q ON q.tag_id = tu.id
                    ORDER BY ld.last_seen DESC
                    """;
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, ubicacionId);
            @SuppressWarnings("unchecked")
            List<Object[]> result = q.getResultList();
            for (Object[] r : result) {
                String tipo = (String) r[0];
                String epc = (String) r[1];
                String nombre = (String) r[2];
                Timestamp ts = (Timestamp) r[3];
                LocalDateTime last = ts == null ? null : ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                rows.add(new Occupant(tipo, epc, nombre == null || nombre.isBlank() ? ("EQUIPMENT".equalsIgnoreCase(tipo) ? ("Equipo (" + epc + ")") : "(Empleado sin nombre)") : nombre, last));
            }
        } finally {
            if (em != null) em.close();
        }
        return rows;
    }
}