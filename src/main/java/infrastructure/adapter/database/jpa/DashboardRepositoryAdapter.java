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
    public List<LocationPresence> fetchPresenceByPrincipalSince(LocalDateTime since) {
        var list = new java.util.ArrayList<LocationPresence>();
        var em = emf.createEntityManager();
        try {
            String sql = """
            /* Suma por ubicación principal: principal + sub */
            WITH last_seen AS (
                SELECT dt.ubicacion_id, dt.epc
                FROM uhf_detection dt
                JOIN (
                    SELECT epc, MAX(created_at) AS last_ts
                    FROM uhf_detection
                    GROUP BY epc
                ) x ON x.epc = dt.epc AND x.last_ts = dt.created_at
                WHERE dt.created_at >= ?1
            ),
            loc_map AS (
                SELECT u.id AS id, COALESCE(u.parent_id, u.id) AS principal_id
                FROM location u
            )
            SELECT
                p.id AS principal_id,
                p.nombre AS principal_name,
                COALESCE(SUM(CASE WHEN e.id  IS NOT NULL THEN 1 ELSE 0 END), 0) AS employees,
                COALESCE(SUM(CASE WHEN eq.id IS NOT NULL THEN 1 ELSE 0 END), 0) AS product
            FROM location p
            LEFT JOIN loc_map m ON m.principal_id = p.id
            LEFT JOIN last_seen ls ON ls.ubicacion_id = m.id
            LEFT JOIN uhf_tag t ON t.epc = ls.epc
            LEFT JOIN poeple e ON e.tag_id = t.id
            LEFT JOIN product eq ON eq.tag_id = t.id
            WHERE p.parent_id IS NULL
            GROUP BY p.id, p.nombre
            ORDER BY p.nombre ASC
        """;
            var q = em.createNativeQuery(sql);
            q.setParameter(1, java.sql.Timestamp.valueOf(since));
            var rows = (java.util.List<Object[]>) q.getResultList();
            for (Object[] r : rows) {
                long id = ((Number) r[0]).longValue();
                String name = (String) r[1];
                int emp = ((Number) r[2]).intValue();
                int eqp = ((Number) r[3]).intValue();
                list.add(new LocationPresence(id, name, emp, eqp));
            }
        } finally { em.close(); }
        return list;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<LocationPresence> fetchPresenceBySubOf(long principalId, LocalDateTime since) {
        var list = new java.util.ArrayList<LocationPresence>();
        var em = emf.createEntityManager();
        try {
            String sql = """
            WITH last_seen AS (
                SELECT dt.ubicacion_id, dt.epc
                FROM uhf_detection dt
                JOIN (
                    SELECT epc, MAX(created_at) AS last_ts
                    FROM uhf_detection
                    GROUP BY epc
                ) x ON x.epc = dt.epc AND x.last_ts = dt.created_at
                WHERE dt.created_at >= ?2
            )
            SELECT
                u.id   AS location_id,
                u.nombre AS location_name,
                COALESCE(SUM(CASE WHEN e.id  IS NOT NULL THEN 1 ELSE 0 END), 0) AS employees,
                COALESCE(SUM(CASE WHEN eq.id IS NOT NULL THEN 1 ELSE 0 END), 0) AS product
            FROM location u
            LEFT JOIN last_seen ls ON ls.ubicacion_id = u.id
            LEFT JOIN uhf_tag t ON t.epc = ls.epc
            LEFT JOIN people e ON e.tag_id = t.id
            LEFT JOIN product eq ON eq.tag_id = t.id
            WHERE (u.parent_id = ?1) OR (u.id = ?1)  -- incluye principal si tiene detecciones directas
            GROUP BY u.id, u.nombre
            ORDER BY u.nombre ASC
        """;
            var q = em.createNativeQuery(sql);
            q.setParameter(1, principalId);
            q.setParameter(2, java.sql.Timestamp.valueOf(since));
            var rows = (java.util.List<Object[]>) q.getResultList();
            for (Object[] r : rows) {
                long id = ((Number) r[0]).longValue();
                String name = (String) r[1];
                int emp = ((Number) r[2]).intValue();
                int eqp = ((Number) r[3]).intValue();
                list.add(new LocationPresence(id, name, emp, eqp));
            }
        } finally { em.close(); }
        return list;
    }

    @Override
    public java.util.List<domain.model.Occupant> fetchOccupantsByUbicacion(long ubicacionId) {
        var rows = new java.util.ArrayList<domain.model.Occupant>();
        var em = emf.createEntityManager();
        try {
            String sql = """
            /* Última detección por EPC en la ubicación indicada (sea principal o sub),
               incluyendo sublocation si se selecciona una principal */
            WITH target AS (
                SELECT id FROM location WHERE id = ?1
                UNION ALL
                SELECT id FROM location WHERE parent_id = ?1
            ),
            last_seen AS (
                SELECT dt.epc, MAX(dt.created_at) AS last_ts
                FROM uhf_detection dt
                WHERE dt.ubicacion_id IN (SELECT id FROM target)
                GROUP BY dt.epc
            )
            SELECT
               CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'PRODUCT' END AS tipo,
               t.epc,
               COALESCE(NULLIF(TRIM(CONCAT(COALESCE(e.full_name,''),' ',COALESCE(e.last_name,''))), ''), eq.nombre) AS nombre,
               ls.last_ts
            FROM last_seen ls
            JOIN uhf_tag t ON t.epc = ls.epc
            LEFT JOIN people e ON e.tag_id = t.id
            LEFT JOIN product eq ON eq.tag_id = t.id
            ORDER BY ls.last_ts DESC
        """;
            var q = em.createNativeQuery(sql);
            q.setParameter(1, ubicacionId);
            java.util.List<Object[]> result = q.getResultList();
            for (Object[] r : result) {
                String tipo = (String) r[0];
                String epc = (String) r[1];
                String nombre = (String) r[2];
                java.sql.Timestamp ts = (java.sql.Timestamp) r[3];
                java.time.LocalDateTime last = ts == null ? null : ts.toLocalDateTime();
                rows.add(new domain.model.Occupant(tipo, epc, (nombre == null || nombre.isBlank()) ? "(" + tipo + " " + epc + ")" : nombre, last));
            }
        } finally { em.close(); }
        return rows;
    }


    @Override
    public int totalEmployees() {
        EntityManager em = emf.createEntityManager();
        try {
            Number n = (Number) em.createNativeQuery("SELECT COUNT(*) FROM people").getSingleResult();
            return n.intValue();
        } finally {
            em.close();
        }
    }

    @Override
    public int totalEquipment() {
        EntityManager em = emf.createEntityManager();
        try {
            Number n = (Number) em.createNativeQuery("SELECT COUNT(*) FROM product").getSingleResult();
            return n.intValue();
        } finally {
            em.close();
        }
    }

}