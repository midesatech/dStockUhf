
package infrastructure.adapter.database.jpa;

import domain.gateway.PermissionRepository;
import domain.model.Permission;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import infrastructure.adapter.database.mysql.entity.PermissionEntity;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaPermissionRepositoryAdapter implements PermissionRepository {
    private final EntityManagerFactory emf;

    public JpaPermissionRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Permission save(Permission permission) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            PermissionEntity entity = PermissionEntity.fromDomain(permission);
            em.persist(entity);
            em.getTransaction().commit();
            return entity.toDomain();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Permission> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM PermissionEntity p", PermissionEntity.class)
                    .getResultList()
                    .stream()
                    .map(PermissionEntity::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Permission> findByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            PermissionEntity entity = em.createQuery(
                            "SELECT p FROM PermissionEntity p WHERE p.name = :name", PermissionEntity.class)
                    .setParameter("name", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(entity != null ? entity.toDomain() : null);
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT COUNT(p) FROM PermissionEntity p", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}
