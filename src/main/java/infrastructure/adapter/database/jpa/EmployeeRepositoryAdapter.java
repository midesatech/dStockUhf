
package infrastructure.adapter.database.jpa;

import domain.gateway.EmployeeRepository;
import domain.model.Employee;
import domain.model.TipoDocumento;
import infrastructure.adapter.database.mysql.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeRepositoryAdapter implements EmployeeRepository {
    private final EntityManagerFactory emf;

    public EmployeeRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Employee save(Employee e) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            EmployeeEntity entity = (e.getId() != null)
                    ? em.find(EmployeeEntity.class, e.getId())
                    : new EmployeeEntity();

            // 🔹 mapeo de atributos directos
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
                UHFTagEntity tag = UHFTagRepositoryHelper.findOrCreateByEpc(
                        em, e.getEpc().trim(), UHFTagEntity.Tipo.EMPLEADO
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
    public List<Employee> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM EmployeeEntity e", EmployeeEntity.class)
                    .getResultList()
                    .stream()
                    .map(EmployeeRepositoryAdapter::toDomain)
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
            EmployeeEntity entity = em.find(EmployeeEntity.class, id);
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
    public Optional<Employee> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            EmployeeEntity entity = em.find(EmployeeEntity.class, id);
            return Optional.ofNullable(entity == null ? null : toDomain(entity));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Employee> search(TipoDocumento tipoDocumento, String numeroDocumento,
                                 String nombre, String apellido, String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT e FROM EmployeeEntity e LEFT JOIN FETCH e.tag t WHERE 1=1");
            List<Object[]> params = new ArrayList<>();

            if (tipoDocumento != null) {
                jpql.append(" AND e.docType = :td");
                params.add(new Object[]{"td", tipoDocumento});
            }
            if (numeroDocumento != null && !numeroDocumento.isBlank()) {
                jpql.append(" AND e.docNumber LIKE :nd");
                params.add(new Object[]{"nd", "%" + numeroDocumento + "%"});
            }
            if (nombre != null && !nombre.isBlank()) {
                jpql.append(" AND LOWER(e.fullName) LIKE :nm");
                params.add(new Object[]{"nm", "%" + nombre.toLowerCase() + "%"});
            }
            if (apellido != null && !apellido.isBlank()) {
                jpql.append(" AND LOWER(e.lastName) LIKE :ap");
                params.add(new Object[]{"ap", "%" + apellido.toLowerCase() + "%"});
            }
            if (epc != null && !epc.isBlank()) {
                jpql.append(" AND t.epc LIKE :epc");
                params.add(new Object[]{"epc", "%" + epc + "%"});
            }

            var q = em.createQuery(jpql.toString(), EmployeeEntity.class);
            for (Object[] p : params) q.setParameter((String)p[0], p[1]);

            return q.getResultList().stream()
                    .map(EmployeeRepositoryAdapter::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Employee> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM EmployeeEntity e " +
                            "JOIN e.tag t " +
                            "WHERE t.epc = :epc",
                    EmployeeEntity.class);
            query.setParameter("epc", epc);
            return query.getResultStream()
                    .findFirst()
                    .map(EmployeeRepositoryAdapter::toDomain);
        } finally {
            em.close();
        }
    }

    // 🔹 Mapper Entity -> Domain
    private static Employee toDomain(EmployeeEntity entity) {
        if (entity == null) return null;
        Employee e = new Employee();
        e.setId(entity.getId());
        e.setFullName(entity.getFullName());
        e.setLastName(entity.getLastName());
        e.setDocType(entity.getDocType());
        e.setDocNumber(entity.getDocNumber());
        e.setBirthDate(entity.getBirthDate());
        e.setBloodType(entity.getBloodType());
        e.setEmail(entity.getEmail());
        e.setPhone(entity.getPhone());
        if (entity.getTag() != null)
            e.setEpc(entity.getTag().getEpc());
        return e;
    }
}