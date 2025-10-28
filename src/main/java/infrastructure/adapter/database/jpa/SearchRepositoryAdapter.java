package infrastructure.adapter.database.jpa;

import domain.gateway.SearchRepository;
import domain.model.DetectionRecord;
import domain.model.Occupant;
import domain.model.PathHop;
import infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

public class SearchRepositoryAdapter implements SearchRepository {

    private final EntityManagerFactory emf;

    public SearchRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public List<Occupant> searchBySubjectAndTime(String subject, LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        List<Occupant> rows = new ArrayList<>();
        try {
            String sql = """
                SELECT
                   CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'EQUIPMENT' END AS tipo,
                   dt.epc,
                   COALESCE(NULLIF(TRIM(CONCAT(COALESCE(e.full_name,''),' ',COALESCE(e.last_name,''))), ''), eq.nombre, CONCAT('EPC ', dt.epc)) AS nombre,
                   MAX(dt.created_at) AS last_seen
                FROM detecciones_tags dt
                LEFT JOIN tags_uhf t  ON t.epc = dt.epc
                LEFT JOIN empleados e ON e.tag_id = t.id
                LEFT JOIN equipment eq ON eq.tag_id = t.id
                WHERE dt.created_at BETWEEN ?1 AND ?2
                  AND (
                      (?3 = 'EMPLOYEE'  AND e.id IS NOT NULL) OR
                      (?3 = 'EQUIPMENT' AND eq.id IS NOT NULL)
                  )
                GROUP BY tipo, dt.epc, nombre
                ORDER BY last_seen DESC
            """;
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, Timestamp.valueOf(start));
            q.setParameter(2, Timestamp.valueOf(end));
            q.setParameter(3, subject);

            List<Object[]> rs = q.getResultList();
            ZoneId Z = ZoneId.systemDefault();
            for (Object[] r : rs) {
                String tipo = (String) r[0];
                String epc  = (String) r[1];
                String nom  = (String) r[2];
                Timestamp ts= (Timestamp) r[3];
                rows.add(new Occupant(tipo, epc, nom,
                        ts == null ? null : ts.toInstant().atZone(Z).toLocalDateTime()));
            }
        } finally { if (em != null) em.close(); }
        return rows;
    }

    @Override
    public List<DetectionRecord> searchRawBySubjectAndTime(String subject, LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        List<DetectionRecord> out = new ArrayList<>();
        try {

            String sql = """
                SELECT
                  CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'EQUIPMENT' END AS tipo,
                  dt.epc,
                  COALESCE(NULLIF(TRIM(CONCAT(COALESCE(e.full_name,''),' ',COALESCE(e.last_name,''))), ''), eq.nombre, CONCAT('EPC ', dt.epc)) AS nombre,
                  u.id AS location_id,
                  u.nombre AS location_name,
                  dt.created_at
                FROM detecciones_tags dt
                LEFT JOIN tags_uhf t  ON t.epc = dt.epc
                LEFT JOIN empleados e ON e.tag_id = t.id
                LEFT JOIN equipment eq ON eq.tag_id = t.id
                LEFT JOIN ubicaciones u ON u.id = dt.ubicacion_id
                WHERE dt.created_at BETWEEN ?1 AND ?2
                  AND (
                      (?3 = 'EMPLOYEE'  AND e.id IS NOT NULL) OR
                      (?3 = 'EQUIPMENT' AND eq.id IS NOT NULL)
                  )
                ORDER BY dt.created_at DESC
            """;
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, Timestamp.valueOf(start));
            q.setParameter(2, Timestamp.valueOf(end));
            q.setParameter(3, subject);

            List<Object[]> rs = q.getResultList();
            ZoneId Z = ZoneId.systemDefault();
            for (Object[] r : rs) {
                String tipo = (String) r[0];
                String epc  = (String) r[1];
                String nom  = (String) r[2];
                Number lid  = (Number) r[3];
                String lname= (String) r[4];
                Timestamp ts= (Timestamp) r[5];
                out.add(new DetectionRecord(
                        tipo, epc, nom,
                        lid == null ? null : lid.longValue(),
                        lname,
                        ts == null ? null : ts.toInstant().atZone(Z).toLocalDateTime()
                ));
            }
        } finally { if (em != null) em.close(); }
        return out;
    }

    @Override
    public List<PathHop> pathForEpc(String epc, LocalDateTime start, LocalDateTime end) {
        EntityManager em = emf.createEntityManager();
        List<PathHop> hops = new ArrayList<>();
        try {
            String sql = """
                SELECT u.id AS location_id, u.nombre AS location_name, dt.created_at
                FROM detecciones_tags dt
                LEFT JOIN ubicaciones u ON u.id = dt.ubicacion_id
                WHERE dt.epc = ?1
                  AND dt.created_at BETWEEN ?2 AND ?3
                ORDER BY dt.created_at ASC
            """;
            Query q = em.createNativeQuery(sql);
            q.setParameter(1, epc);
            q.setParameter(2, Timestamp.valueOf(start));
            q.setParameter(3, Timestamp.valueOf(end));

            List<Object[]> rs = q.getResultList();
            ZoneId Z = ZoneId.systemDefault();

            Long curId = null; String curName = null;
            LocalDateTime first = null, last = null;
            int count = 0;

            for (Object[] r : rs) {
                Number lid  = (Number) r[0];
                String lname= (String) r[1];
                Timestamp ts= (Timestamp) r[2];
                LocalDateTime t = ts == null ? null : ts.toInstant().atZone(Z).toLocalDateTime();
                Long id = (lid == null) ? null : lid.longValue();

                if (!Objects.equals(id, curId)) {
                    if (curId != null) hops.add(new PathHop(curId, curName, first, last, count));
                    curId = id; curName = lname; first = t; last = t; count = 1;
                } else { last = t; count++; }
            }
            if (curId != null) hops.add(new PathHop(curId, curName, first, last, count));
        } finally { if (em != null) em.close(); }
        return hops;
    }

    @Override
    public java.util.List<domain.model.Occupant> searchBySubjectAndTimeAt(String subject, java.time.LocalDateTime start, java.time.LocalDateTime end, Long ubicacionId) {
        var em = emf.createEntityManager();
        var rows = new java.util.ArrayList<domain.model.Occupant>();
        try {
            String scope = (ubicacionId == null) ? "" : """
            WITH target AS (
                SELECT id FROM ubicaciones WHERE id = :uid
                UNION ALL
                SELECT id FROM ubicaciones WHERE parent_id = :uid
            )
        """;
            String inClause = (ubicacionId == null) ? "" : "AND dt.ubicacion_id IN (SELECT id FROM target)";

            String sql = scope + """
            SELECT
               CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'EQUIPMENT' END AS tipo,
               dt.epc,
               COALESCE(NULLIF(TRIM(CONCAT(COALESCE(e.full_name,''),' ',COALESCE(e.last_name,''))), ''), eq.name) AS nombre,
               MAX(dt.created_at) as last_ts
            FROM detecciones_tags dt
            LEFT JOIN tags_uhf t ON t.epc = dt.epc
            LEFT JOIN empleados e ON e.tag_id = t.id
            LEFT JOIN equipment eq ON eq.tag_id = t.id
            WHERE dt.created_at BETWEEN :s AND :e
              AND (CASE WHEN :subject = 'EMPLOYEE' THEN e.id IS NOT NULL ELSE eq.id IS NOT NULL END)
              """ + inClause + """
            GROUP BY 1,2,3
            ORDER BY last_ts DESC
        """;
            var q = em.createNativeQuery(sql);
            q.setParameter("s", java.sql.Timestamp.valueOf(start));
            q.setParameter("e", java.sql.Timestamp.valueOf(end));
            q.setParameter("subject", subject);
            if (ubicacionId != null) q.setParameter("uid", ubicacionId);

            java.util.List<Object[]> rs = q.getResultList();
            for (Object[] r : rs) {
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
    public java.util.List<domain.model.DetectionRecord> searchRawBySubjectAndTimeAt(
            String subject,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end,
            Long ubicacionId
    ) {
        var em = emf.createEntityManager();
        var rows = new java.util.ArrayList<domain.model.DetectionRecord>();
        try {
            String scope = (ubicacionId == null) ? "" : """
            WITH target AS (
                SELECT id FROM ubicaciones WHERE id = :uid
                UNION ALL
                SELECT id FROM ubicaciones WHERE parent_id = :uid
            )
        """;
            // Si filtras por ubicación, restringe a target; si no, usa todo.
            String inClause = (ubicacionId == null) ? "" : "AND dt.ubicacion_id IN (SELECT id FROM target)";

            // NOTA: agregamos u.id AS ubicacion_id y un "source" string (placeholder vacío '')
            String sql = scope + """
            SELECT
               CASE WHEN e.id IS NOT NULL THEN 'EMPLOYEE' ELSE 'EQUIPMENT' END AS tipo,
               dt.epc,
               COALESCE(u.nombre, '(Sin ubicación)') AS ubicacion,
               u.id AS ubicacion_id,
               '' AS source,            -- ajusta aquí si tienes un campo de origen (lector/dispositivo)
               dt.created_at
            FROM detecciones_tags dt
            LEFT JOIN ubicaciones u ON u.id = dt.ubicacion_id
            LEFT JOIN tags_uhf t ON t.epc = dt.epc
            LEFT JOIN empleados e ON e.tag_id = t.id
            LEFT JOIN equipment eq ON eq.tag_id = t.id
            WHERE dt.created_at BETWEEN :s AND :e
              AND (CASE WHEN :subject = 'EMPLOYEE' THEN e.id IS NOT NULL ELSE eq.id IS NOT NULL END)
              """ + inClause + """
            ORDER BY dt.created_at DESC
        """;

            var q = em.createNativeQuery(sql);
            q.setParameter("s", java.sql.Timestamp.valueOf(start));
            q.setParameter("e", java.sql.Timestamp.valueOf(end));
            q.setParameter("subject", subject);
            if (ubicacionId != null) q.setParameter("uid", ubicacionId);

            @SuppressWarnings("unchecked")
            java.util.List<Object[]> rs = q.getResultList();

            for (Object[] r : rs) {
                String tipo = (String) r[0];
                String epc = (String) r[1];
                String ubiNombre = (String) r[2];
                Long ubiId = (r[3] == null) ? null : ((Number) r[3]).longValue();
                String source = (String) r[4]; // placeholder: '' (vacío) por ahora
                java.sql.Timestamp ts = (java.sql.Timestamp) r[5];
                java.time.LocalDateTime when = (ts == null) ? null : ts.toLocalDateTime();

                rows.add(new domain.model.DetectionRecord(
                        tipo,           // String
                        epc,            // String
                        ubiNombre,      // String
                        ubiId,          // Long
                        source,         // String
                        when            // LocalDateTime
                ));
            }
        } finally {
            em.close();
        }
        return rows;
    }
}