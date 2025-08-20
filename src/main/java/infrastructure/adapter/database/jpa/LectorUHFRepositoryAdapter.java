package infrastructure.adapter.database.jpa;

import domain.gateway.LectorUHFRepository;
import domain.model.LectorUHF;
import domain.model.Ubicacion;
import infrastructure.adapter.database.mysql.entity.LectorUHFEntity;
import infrastructure.adapter.database.mysql.entity.UbicacionEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LectorUHFRepositoryAdapter implements LectorUHFRepository {

    private final EntityManagerFactory emf;

    public LectorUHFRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public LectorUHF save(LectorUHF l) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            LectorUHFEntity e =
                    new LectorUHFEntity();
            e.setCodigo(l.getCodigo());
            e.setDescripcion(l.getDescripcion());

            if (l.getUbicacion() == null || l.getUbicacion().getId() == null) {
                throw new IllegalArgumentException("Ubicación requerida para el lector UHF");
            }
            UbicacionEntity ubRef = em.getReference(UbicacionEntity.class, l.getUbicacion().getId());
            e.setUbicacion(ubRef);

            em.persist(e);
            tx.commit();

            return toDomain(e);

        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            if (ex.getCause() != null && ex.getCause().getMessage().contains("Duplicate entry")) {
                throw new domain.exception.DuplicateFieldException(
                        "Ya existe un lector con este código"
                );
            }

            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<LectorUHF> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<LectorUHFEntity> list =
                    em.createQuery("select l from LectorUHFEntity l", LectorUHFEntity.class)
                            .getResultList();
            return list.stream().map(this::toDomain).collect(Collectors.toList());
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
            LectorUHFEntity e =
                    em.find(LectorUHFEntity.class, id);
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
    public Optional<LectorUHF> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            LectorUHFEntity e =
                    em.find(LectorUHFEntity.class, id);
            return Optional.ofNullable(e == null ? null : toDomain(e));
        } finally {
            em.close();
        }
    }

    @Override
    public LectorUHF update(LectorUHF l) {
        if (l.getId() == null) throw new IllegalArgumentException("ID requerido para actualizar lector UHF");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            LectorUHFEntity e =
                    em.find(LectorUHFEntity.class, l.getId());
            if (e == null) throw new IllegalArgumentException("Lector UHF no encontrado con id " + l.getId());

            e.setCodigo(l.getCodigo());
            e.setDescripcion(l.getDescripcion());

            if (l.getUbicacion() == null || l.getUbicacion().getId() == null) {
                throw new IllegalArgumentException("Ubicación requerida para el lector UHF");
            }
            UbicacionEntity ubRef = em.getReference(UbicacionEntity.class, l.getUbicacion().getId());
            e.setUbicacion(ubRef);

            em.merge(e);
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
    public Optional<LectorUHF> findByCodigo(String codigo) {
        EntityManager em = emf.createEntityManager();
        try {
            List<LectorUHFEntity> list =
                    em.createQuery("select l from LectorUHFEntity l where l.codigo = :codigo",
                                    LectorUHFEntity.class)
                            .setParameter("codigo", codigo)
                            .setMaxResults(1)
                            .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(toDomain(list.get(0)));
        } finally {
            em.close();
        }
    }

    private LectorUHF toDomain(LectorUHFEntity e) {
        UbicacionEntity ue = e.getUbicacion();
        Ubicacion u = (ue == null) ? null : new Ubicacion(ue.getId(), ue.getNombre());
        return new LectorUHF(e.getId(), e.getCodigo(), e.getDescripcion(), u);
    }

    @Override
    public List<LectorUHF> findByFilters(String codigo, Long ubicacionId) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("select l from LectorUHFEntity l where 1=1");

            if (codigo != null && !codigo.isBlank()) {
                jpql.append(" and lower(l.codigo) like lower(:codigo)");
            }
            if (ubicacionId != null) {
                jpql.append(" and l.ubicacion.id = :ubicacionId");
            }

            var query = em.createQuery(jpql.toString(), LectorUHFEntity.class);

            if (codigo != null && !codigo.isBlank()) {
                query.setParameter("codigo", "%" + codigo + "%");
            }
            if (ubicacionId != null) {
                query.setParameter("ubicacionId", ubicacionId);
            }

            List<LectorUHFEntity> list = query.getResultList();
            return list.stream().map(this::toDomain).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<LectorUHF> buscar(String codigo, Ubicacion ubicacion) {
        Long ubicacionId = (ubicacion != null) ? ubicacion.getId() : null;
        return findByFilters(codigo, ubicacionId);
    }
}