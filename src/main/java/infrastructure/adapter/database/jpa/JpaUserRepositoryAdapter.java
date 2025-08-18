
package infrastructure.adapter.database.jpa;

import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepository;
import domain.model.User;
import infrastructure.adapter.database.mysql.entity.UserEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaUserRepositoryAdapter implements UserRepository {
    private final EntityManagerFactory emf;
    private final PasswordEncoderPort encoder;

    public JpaUserRepositoryAdapter(EntityManagerFactory emf, PasswordEncoderPort encoder) {
        this.emf = emf;
        this.encoder = encoder;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        EntityManager em = emf.createEntityManager();
        try {
            UserEntity ue = em.createQuery(
                            "select u from UserEntity u where upper(u.username)=upper(:u)",
                            UserEntity.class
                    )
                    .setParameter("u", username)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            return Optional.ofNullable(EntityMappers.toDomain(ue)); // 🔹 con roles+permisos completos
        } finally {
            em.close();
        }
    }

    @Override
    public User save(User user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            UserEntity e = EntityMappers.toEntity(user);

            // 🔹 Limpio roles previos (por si viene un update con roles distintos)
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                e.setRoles(
                        user.getRoles().stream()
                                .map(r -> em.getReference(
                                        infrastructure.adapter.database.mysql.entity.RoleEntity.class,
                                        r.getId()
                                ))
                                .collect(Collectors.toSet())
                );
            } else {
                e.setRoles(null);
            }

            UserEntity merged = em.merge(e);
            tx.commit();

            user.setId(merged.getId());
            return EntityMappers.toDomain(merged);

        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
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
            UserEntity e = em.find(UserEntity.class, id);

            if (e != null && e.isSystemUser()) {
                throw new IllegalStateException("No se puede eliminar el usuario ADMIN");
            }

            if (e != null) em.remove(e);

            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public Optional<User> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            UserEntity e = em.find(UserEntity.class, id);
            return Optional.ofNullable(EntityMappers.toDomain(e)); // 🔹 completo
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select count(u) from UserEntity u", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select u from UserEntity u", UserEntity.class)
                    .getResultList()
                    .stream()
                    .map(EntityMappers::toDomain)
                    .toList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<User> findAllLight() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select u from UserEntity u", UserEntity.class)
                    .getResultList()
                    .stream()
                    .map(EntityMappers::toDomainLight) // 🔹 liviano: roles sin permisos
                    .toList();
        } finally {
            em.close();
        }
    }
}