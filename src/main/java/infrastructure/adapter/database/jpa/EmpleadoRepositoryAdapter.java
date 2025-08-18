
package infrastructure.adapter.database.jpa;

import domain.gateway.EmpleadoGateway;
import domain.model.Empleado;
import infrastructure.adapter.database.mysql.entity.EmpleadoEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmpleadoRepositoryAdapter implements EmpleadoGateway {
    private final EntityManagerFactory emf;

    public EmpleadoRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // ==== mapeos ====
    private static Empleado toDomain(EmpleadoEntity e) {
        if (e == null) return null;
        return new Empleado(
                e.getId(),
                e.getCodigo(),
                e.getFullName(),
                e.getLastName(),
                e.getDocType(),
                e.getDocNumber(),
                e.getBirthDate(),
                e.getBloodType(),
                e.getEmail(),
                e.getPhone()
        );
    }

    private static void copyToEntity(Empleado src, EmpleadoEntity dst) {
        dst.setCodigo(trimOrNull(src.getCodigo()));
        dst.setFullName(src.getFullName() != null ? src.getFullName().trim() : null);
        dst.setLastName(src.getLastName() != null ? src.getLastName().trim() : null);
        dst.setDocType(src.getTipoDocumento());
        dst.setDocNumber(src.getNumeroDocumento() != null ? src.getNumeroDocumento().trim() : null);
        dst.setBirthDate(src.getFechaNacimiento());
        dst.setBloodType(src.getTipoSanguineo());
        dst.setEmail(trimOrNull(lowerOrNull(src.getEmail())));
        dst.setPhone(trimOrNull(src.getTelefono()));
    }

    private static String trimOrNull(String s) { return s == null ? null : s.trim(); }
    private static String lowerOrNull(String s) { return s == null ? null : s.toLowerCase(); }

    @Override
    public Empleado save(Empleado e) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            EmpleadoEntity entity;
            if (e.getId() != null) {
                entity = em.find(EmpleadoEntity.class, e.getId());
                if (entity == null) throw new IllegalArgumentException("Empleado no encontrado");
            } else {
                entity = new EmpleadoEntity();
            }

            copyToEntity(e, entity);

            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                // entity ya está managed; opcionalmente em.merge(entity);
            }

            tx.commit();
            return toDomain(entity);

        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Empleado> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select e from EmpleadoEntity e", EmpleadoEntity.class)
                    .getResultList()
                    .stream()
                    .map(EmpleadoRepositoryAdapter::toDomain)
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
            EmpleadoEntity e = em.find(EmpleadoEntity.class, id);
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
    public Optional<Empleado> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            EmpleadoEntity e = em.find(EmpleadoEntity.class, id);
            return Optional.ofNullable(toDomain(e));
        } finally {
            em.close();
        }
    }
}
