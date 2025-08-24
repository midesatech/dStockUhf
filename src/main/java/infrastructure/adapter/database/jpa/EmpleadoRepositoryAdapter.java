
package infrastructure.adapter.database.jpa;

import domain.gateway.EmpleadoGateway;
import domain.model.Empleado;
import domain.model.TipoDocumento;
import infrastructure.adapter.database.mysql.entity.*;
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

    @Override
    public Empleado save(Empleado e) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            EmpleadoEntity entity = (e.getId() != null)
                    ? em.find(EmpleadoEntity.class, e.getId())
                    : new EmpleadoEntity();

            // 🔹 mapeo de atributos directos
            entity.setCodigo(e.getCodigo());
            entity.setFullName(e.getFullName());
            entity.setLastName(e.getLastName());
            entity.setDocType(e.getDocType());
            entity.setDocNumber(e.getDocNumber());
            entity.setBirthDate(e.getBirthDate());
            entity.setBloodType(e.getBloodType());
            entity.setEmail(e.getEmail());
            entity.setPhone(e.getPhone());

            // 🔹 EPC -> TagUHFEntity (TIPO = EMPLEADO)
            if (e.getEpc() != null && !e.getEpc().isBlank()) {
                TagUHFEntity tag = TagUHFRepositoryHelper.findOrCreateByEpc(
                        em, e.getEpc().trim(), TagUHFEntity.Tipo.EMPLEADO
                );                entity.setTag(tag);
            } else {
                entity.setTag(null);
            }

            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
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
            return em.createQuery("SELECT e FROM EmpleadoEntity e", EmpleadoEntity.class)
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
            EmpleadoEntity entity = em.find(EmpleadoEntity.class, id);
            if (entity != null) em.remove(entity);
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
            EmpleadoEntity entity = em.find(EmpleadoEntity.class, id);
            return Optional.ofNullable(entity == null ? null : toDomain(entity));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Empleado> search(TipoDocumento tipoDocumento, String numeroDocumento,
                                 String nombre, String apellido, String codigo) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM EmpleadoEntity e WHERE 1=1";
            if (tipoDocumento != null) jpql += " AND e.docType = :docType";
            if (numeroDocumento != null && !numeroDocumento.isBlank()) jpql += " AND e.docNumber LIKE :docNumber";
            if (nombre != null && !nombre.isBlank()) jpql += " AND e.fullName LIKE :nombre";
            if (apellido != null && !apellido.isBlank()) jpql += " AND e.lastName LIKE :apellido";
            if (codigo != null && !codigo.isBlank()) jpql += " AND e.codigo LIKE :codigo";

            var q = em.createQuery(jpql, EmpleadoEntity.class);
            if (tipoDocumento != null) q.setParameter("docType", tipoDocumento);
            if (numeroDocumento != null && !numeroDocumento.isBlank()) q.setParameter("docNumber", "%" + numeroDocumento + "%");
            if (nombre != null && !nombre.isBlank()) q.setParameter("nombre", "%" + nombre + "%");
            if (apellido != null && !apellido.isBlank()) q.setParameter("apellido", "%" + apellido + "%");
            if (codigo != null && !codigo.isBlank()) q.setParameter("codigo", "%" + codigo + "%");

            return q.getResultList().stream()
                    .map(EmpleadoRepositoryAdapter::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Empleado> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM EmpleadoEntity e " +
                            "JOIN e.tag t " +
                            "WHERE t.epc = :epc",
                    EmpleadoEntity.class);
            query.setParameter("epc", epc);
            return query.getResultStream()
                    .findFirst()
                    .map(EmpleadoRepositoryAdapter::toDomain);
        } finally {
            em.close();
        }
    }

    // 🔹 Mapper Entity -> Domain
    private static Empleado toDomain(EmpleadoEntity entity) {
        Empleado e = new Empleado();
        e.setId(entity.getId());
        e.setCodigo(entity.getCodigo());   // ← se mantiene
        e.setFullName(entity.getFullName());
        e.setLastName(entity.getLastName());
        e.setDocType(entity.getDocType());
        e.setDocNumber(entity.getDocNumber());
        e.setBirthDate(entity.getBirthDate());
        e.setBloodType(entity.getBloodType());
        e.setEmail(entity.getEmail());
        e.setPhone(entity.getPhone());
        if (entity.getTag() != null)
            e.setEpc(entity.getTag().getEpc()); // ← epc desde TagUHF
        return e;
    }
}