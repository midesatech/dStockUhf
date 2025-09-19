package infrastructure.adapter.database.jpa;

import domain.gateway.ScanRepository;
import domain.model.UHFReader;
import domain.model.Ubicacion;
import domain.model.tag.TagScan;
import domain.model.tag.TagScanFilter;
import infrastructure.adapter.database.mysql.entity.LocationEntity;
import infrastructure.adapter.database.mysql.entity.TagDetectionEntity;
import infrastructure.adapter.database.mysql.entity.UHFReaderEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.List;

public class ScansRepositoryAdapter implements ScanRepository {

    private final EntityManagerFactory emf;

    public ScansRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // --- mapping helpers ---
    private static UHFReader toDomain(UHFReaderEntity e) {
        if (e == null) return null;
        Ubicacion u = (e.getUbicacion() != null)
                ? new Ubicacion(e.getUbicacion().getId(), e.getUbicacion().getNombre())
                : null;
        return new UHFReader(
                e.getId(),
                e.getCodigo(),
                e.getDescripcion(),
                u
        );
    }

    private static TagScan toDomain(TagDetectionEntity e) {
        Ubicacion u = (e.getUbicacion() != null)
                ? new Ubicacion(e.getUbicacion().getId(), e.getUbicacion().getNombre())
                : null;
        return new TagScan(
                e.getId(),
                toDomain(e.getLector()),
                u,
                e.getEpc(),
                e.getRssi(),
                e.getMachine(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private static void applyFilters(StringBuilder jpql, List<Object[]> params, TagScanFilter f) {
        jpql.append(" WHERE 1=1 ");
        if (f != null) {
            if (f.epc != null && !f.epc.isBlank()) {
                jpql.append(" AND UPPER(d.epc) LIKE :epc ");
                params.add(new Object[]{"epc", "%" + f.epc.trim().toUpperCase() + "%"});
            }
            if (f.lectorCodigo != null && !f.lectorCodigo.isBlank()) {
                jpql.append(" AND UPPER(l.codigo) = :codigo ");
                params.add(new Object[]{"codigo", f.lectorCodigo.trim().toUpperCase()});
            }
            if (f.ubicacionId != null) {
                // Coincide si la detección tiene ubicación explícita
                // o si el lector está asociado a esa ubicación.
                jpql.append(" AND ( (u.id = :uid) OR (lu.id = :uid) ) ");
                params.add(new Object[]{"uid", f.ubicacionId});
            }
            if (f.desde != null) {
                jpql.append(" AND d.createdAt >= :desde ");
                params.add(new Object[]{"desde", f.desde});
            }
            if (f.hasta != null) {
                jpql.append(" AND d.createdAt <= :hasta ");
                params.add(new Object[]{"hasta", f.hasta});
            }
            if (f.rssiMin != null) {
                jpql.append(" AND (d.rssi IS NULL OR d.rssi >= :rssiMin) ");
                params.add(new Object[]{"rssiMin", f.rssiMin});
            }
            if (f.machine != null && !f.machine.isBlank()) {
                jpql.append(" AND UPPER(d.machine) LIKE :machine ");
                params.add(new Object[]{"machine", "%" + f.machine.trim().toUpperCase() + "%"});
            }
        }
        jpql.append(" ORDER BY d.createdAt DESC ");
    }

    @Override
    public TagScan save(TagScan scan) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            TagDetectionEntity e = new TagDetectionEntity();

            // lector (obligatorio)
            UHFReaderEntity lector = em.createQuery(
                            "SELECT r FROM UHFReaderEntity r WHERE UPPER(r.codigo) = :cod", UHFReaderEntity.class)
                    .setParameter("cod", scan.getLector().getCodigo().trim().toUpperCase())
                    .getResultStream().findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Lector no encontrado por código"));

            e.setLector(lector);

            // ubicación opcional (si no viene, se deja null y se puede inferir en reportes)
            if (scan.getUbicacion() != null && scan.getUbicacion().getId() != null) {
                LocationEntity u = em.find(LocationEntity.class, scan.getUbicacion().getId());
                e.setUbicacion(u);
            }

            e.setEpc(scan.getEpc());
            e.setRssi(scan.getRssi());
            e.setMachine(scan.getMachine());

            em.persist(e);
            tx.commit();
            return toDomain(e);
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<TagScan> findByFilters(TagScanFilter f) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT d FROM TagDetectionEntity d " +
                            "JOIN d.lector l " +
                            "LEFT JOIN d.ubicacion u " +
                            "LEFT JOIN l.ubicacion lu "
            );
            List<Object[]> params = new ArrayList<>();
            applyFilters(jpql, params, f);

            TypedQuery<TagDetectionEntity> q = em.createQuery(jpql.toString(), TagDetectionEntity.class);
            for (Object[] p : params) q.setParameter((String)p[0], p[1]);
            q.setMaxResults((f != null && f.limit > 0) ? f.limit : 500);

            return q.getResultList().stream().map(ScansRepositoryAdapter::toDomain).toList();
        } finally {
            em.close();
        }
    }
}
