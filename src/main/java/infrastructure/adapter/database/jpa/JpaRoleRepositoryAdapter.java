
package infrastructure.adapter.database.jpa;

import domain.model.Permission;
import domain.model.Role;
import domain.gateway.RoleRepository;
import infrastructure.adapter.database.mysql.entity.PermissionEntity;
import infrastructure.adapter.database.mysql.entity.RoleEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JpaRoleRepositoryAdapter implements RoleRepository {
    private final EntityManagerFactory emf;

    public JpaRoleRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private Role toDomain(RoleEntity e) {
        return new Role(
                e.getId(),
                e.getName(),
                e.getPermissions()
                        .stream()
                        .map(p -> new Permission(p.getId(), p.getName()))
                        .collect(Collectors.toSet())
        );
    }

    private RoleEntity toEntity(Role r) {
        RoleEntity e = new RoleEntity();
        e.setId(r.getId());
        e.setName(r.getName());
        e.setPermissions(
                r.getPermissions().stream()
                        .map(p -> {
                            PermissionEntity pe = new PermissionEntity();
                            pe.setId(p.getId());
                            pe.setName(p.getName());
                            return pe;
                        }).collect(Collectors.toSet())
        );
        return e;
    }

    @Override
    public List<Role> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select r from RoleEntity r", RoleEntity.class)
                    .getResultList()
                    .stream()
                    .map(this::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Role> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            RoleEntity e = em.find(RoleEntity.class, id);
            return e != null ? Optional.of(toDomain(e)) : Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public Role save(Role role) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Resolver la entidad a actualizar/crear
            RoleEntity e;
            if (role.getId() != null) {
                e = em.find(RoleEntity.class, role.getId());
                if (e == null) {
                    e = new RoleEntity(); // si no existe, crea una nueva
                }
            } else {
                e = new RoleEntity();
            }

            e.setName(role.getName());

            // Sin lambda -> no hay problema de "efectivamente final"
            e.getPermissions().clear();
            for (Permission p : role.getPermissions()) {
                PermissionEntity pe = em.createQuery(
                                "select p from PermissionEntity p where p.name = :n", PermissionEntity.class)
                        .setParameter("n", p.getName())
                        .getResultStream()
                        .findFirst()
                        .orElseGet(() -> {
                            PermissionEntity newPe = new PermissionEntity();
                            newPe.setName(p.getName());
                            em.persist(newPe);
                            return newPe;
                        });
                e.getPermissions().add(pe);
            }

            // Persistir o actualizar
            if (e.getId() == null) {
                em.persist(e);
            } else {
                e = em.merge(e); // opcional si e ya está managed, pero seguro si llegó "nuevo" con id
            }

            tx.commit();
            return toDomain(e);
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            RoleEntity e = em.find(RoleEntity.class, id);
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
    public Optional<Role> findByName(String name) {
        EntityManager em = emf.createEntityManager();
        try {
            RoleEntity e = em.createQuery("select r from RoleEntity r where r.name = :n", RoleEntity.class)
                    .setParameter("n", name)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return e != null ? Optional.of(toDomain(e)) : Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select count(r) from RoleEntity r", Long.class)
                    .getSingleResult();
        } finally {
            em.close();
        }
    }
}