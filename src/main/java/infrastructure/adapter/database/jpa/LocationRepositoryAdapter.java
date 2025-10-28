
package infrastructure.adapter.database.jpa;

import domain.gateway.LocationRepository;
import domain.model.Ubicacion;
import infrastructure.adapter.database.mysql.entity.LocationEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class LocationRepositoryAdapter implements LocationRepository {
    private final EntityManagerFactory emf;

    public LocationRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private static Ubicacion map(LocationEntity e) {
        return new Ubicacion(
                e.getId(),
                e.getNombre(),
                e.getParent() == null ? null : e.getParent().getId(),
                e.getParent() == null ? null : e.getParent().getNombre()
        );
    }

    @Override
    public Ubicacion save(Ubicacion u) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LocationEntity e;
            if (u.getId() != null) {
                e = em.find(LocationEntity.class, u.getId());
                e.setNombre(u.getNombre());
            } else {
                e = new LocationEntity();
                e.setNombre(u.getNombre());
            }
            if (u.getParentId() != null) {
                LocationEntity parent = em.find(LocationEntity.class, u.getParentId());
                e.setParent(parent);
            } else {
                e.setParent(null);
            }
            if (e.getId() == null) em.persist(e);
            tx.commit();
            return map(e);
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Ubicacion> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<LocationEntity> rows = em.createQuery(
                    "SELECT u FROM LocationEntity u LEFT JOIN FETCH u.parent ORDER BY COALESCE(u.parent.id, u.id), u.nombre",
                    LocationEntity.class).getResultList();
            return rows.stream().map(LocationRepositoryAdapter::map).collect(Collectors.toList());
        } finally { em.close(); }
    }

    @Override
    public List<Ubicacion> findPrincipals() {
        EntityManager em = emf.createEntityManager();
        try {
            List<LocationEntity> rows = em.createQuery(
                    "SELECT u FROM LocationEntity u WHERE u.parent IS NULL ORDER BY u.nombre",
                    LocationEntity.class).getResultList();
            return rows.stream().map(LocationRepositoryAdapter::map).collect(Collectors.toList());
        } finally { em.close(); }
    }

    @Override
    public List<Ubicacion> findByParentId(Long parentId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<LocationEntity> rows = em.createQuery(
                    "SELECT u FROM LocationEntity u LEFT JOIN FETCH u.parent WHERE u.parent.id = :pid ORDER BY u.nombre",
                    LocationEntity.class).setParameter("pid", parentId).getResultList();
            return rows.stream().map(LocationRepositoryAdapter::map).collect(Collectors.toList());
        } finally { em.close(); }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LocationEntity e = em.find(LocationEntity.class, id);
            if (e != null) em.remove(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public Optional<Ubicacion> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            LocationEntity e = em.find(LocationEntity.class, id);
            if (e != null && e.getParent() != null) e.getParent().getId(); // touch
            return Optional.ofNullable(e == null ? null : map(e));
        } finally { em.close(); }
    }
}
