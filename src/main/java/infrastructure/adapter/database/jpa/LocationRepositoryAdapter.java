
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

    @Override
    public Ubicacion save(Ubicacion u) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LocationEntity e = new LocationEntity();
            e.setNombre(u.getNombre());
            em.persist(e);
            tx.commit();
            return new Ubicacion(e.getId(), e.getNombre());
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
            return em.createQuery("select u from LocationEntity u", LocationEntity.class)
                    .getResultList()
                    .stream()
                    .map(e -> new Ubicacion(e.getId(), e.getNombre()))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
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
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Ubicacion> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            LocationEntity e = em.find(LocationEntity.class, id);
            return Optional.ofNullable(e == null ? null : new Ubicacion(e.getId(), e.getNombre()));
        } finally {
            em.close();
        }
    }
}
