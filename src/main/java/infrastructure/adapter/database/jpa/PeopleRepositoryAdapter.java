
package infrastructure.adapter.database.jpa;

import domain.gateway.PeopleRepository;
import domain.model.*;
import infrastructure.adapter.database.mysql.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PeopleRepositoryAdapter implements PeopleRepository {
    private final EntityManagerFactory emf;

    public PeopleRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public People save(People e) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            PeopleEntity entity = (e.getId() != null)
                    ? em.find(PeopleEntity.class, e.getId())
                    : new PeopleEntity();

            // 🔹 mapeo de atributos directos
            entity.setFullName(e.getFullName());
            entity.setLastName(e.getLastName());
            entity.setDocType(e.getDocType().toString());
            entity.setDocNumber(e.getDocNumber());
            entity.setBirthDate(e.getBirthDate());
            entity.setBloodType(e.getBloodType().toString());
            entity.setEmail(e.getEmail());
            entity.setPhone(e.getPhone());

            if (e.getPeopleType()==null || e.getPeopleType().getId()==null)
                throw new IllegalArgumentException("Tipo de persona es requerido");
            PeopleTypeEntity tipo = em.find(PeopleTypeEntity.class, e.getPeopleType().getId());
            if (tipo==null) throw new IllegalArgumentException("Tipo de empleado inválido");
            entity.setPeopleType(tipo);

            // 🔹 EPC -> TagUHFEntity (TIPO = EMPLEADO)
            if (e.getEpc() != null && !e.getEpc().isBlank()) {
                UHFTagEntity tag = UHFTagRepositoryHelper.findOrCreateByEpc(
                        em, e.getEpc().trim(), UHFTag.Tipo.EMPLOYEE.toString()
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
    public List<People> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM PeopleEntity e", PeopleEntity.class)
                    .getResultList()
                    .stream()
                    .map(PeopleRepositoryAdapter::toDomain)
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
            PeopleEntity entity = em.find(PeopleEntity.class, id);
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
    public Optional<People> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            PeopleEntity entity = em.find(PeopleEntity.class, id);
            return Optional.ofNullable(entity == null ? null : toDomain(entity));
        } finally {
            em.close();
        }
    }

    @Override
    public List<People> search(PeopleType peopleType, TipoDocumento tipoDocumento, String numeroDocumento,
                               String nombre, String apellido, String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "SELECT e FROM PeopleEntity e " +
                            "LEFT JOIN FETCH e.tag t " +
                            "LEFT JOIN FETCH e.peopleType pt WHERE 1=1");

            List<Object[]> params = new ArrayList<>();

            // 🔹 Filtro por tipo de persona (si viene)
            if (peopleType != null && peopleType.getId() != null) {
                jpql.append(" AND pt.id = :ptId");
                params.add(new Object[]{"ptId", peopleType.getId()});
            }

            if (tipoDocumento != null) {
                jpql.append(" AND e.docType = :td");
                params.add(new Object[]{"td", tipoDocumento.toString()});
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

            var q = em.createQuery(jpql.toString(), PeopleEntity.class);
            for (Object[] p : params) q.setParameter((String)p[0], p[1]);

            return q.getResultList().stream()
                    .map(PeopleRepositoryAdapter::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<People> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM PeopleEntity e " +
                            "JOIN e.tag t " +
                            "WHERE t.epc = :epc",
                    PeopleEntity.class);
            query.setParameter("epc", epc);
            return query.getResultStream()
                    .findFirst()
                    .map(PeopleRepositoryAdapter::toDomain);
        } finally {
            em.close();
        }
    }

    // 🔹 Mapper Entity -> Domain
    private static People toDomain(PeopleEntity entity) {
        if (entity == null) return null;
        People e = new People();
        e.setId(entity.getId());
        e.setFullName(entity.getFullName());
        e.setLastName(entity.getLastName());
        e.setDocType(TipoDocumento.valueOf(entity.getDocType()));
        e.setDocNumber(entity.getDocNumber());
        e.setBirthDate(entity.getBirthDate());
        e.setBloodType(TipoSangre.valueOf(entity.getBloodType()));
        e.setEmail(entity.getEmail());
        e.setPhone(entity.getPhone());
        if (entity.getPeopleType()!=null)
            e.setPeopleType(new domain.model.PeopleType(entity.getPeopleType().getId(), entity.getPeopleType().getNombre()));
        if (entity.getTag() != null)
            e.setEpc(entity.getTag().getEpc());
        return e;
    }
}