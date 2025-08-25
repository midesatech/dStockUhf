package infrastructure.adapter.database.jpa;

import domain.gateway.UHFReaderRepository;
import domain.model.UHFReader;
import domain.model.Ubicacion;
import infrastructure.adapter.database.mysql.entity.UHFReaderEntity;
import infrastructure.adapter.database.mysql.entity.LocationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UHFReaderRepositoryAdapter implements UHFReaderRepository {

    private final EntityManagerFactory emf;

    public UHFReaderRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public UHFReader save(UHFReader l) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            UHFReaderEntity e =
                    new UHFReaderEntity();
            e.setCodigo(l.getCodigo());
            e.setDescripcion(l.getDescripcion());

            if (l.getUbicacion() == null || l.getUbicacion().getId() == null) {
                throw new IllegalArgumentException("Ubicación requerida para el lector UHF");
            }
            LocationEntity ubRef = em.getReference(LocationEntity.class, l.getUbicacion().getId());
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
    public List<UHFReader> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<UHFReaderEntity> list =
                    em.createQuery("select l from UHFReaderEntity l", UHFReaderEntity.class)
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
            UHFReaderEntity e =
                    em.find(UHFReaderEntity.class, id);
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
    public Optional<UHFReader> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            UHFReaderEntity e =
                    em.find(UHFReaderEntity.class, id);
            return Optional.ofNullable(e == null ? null : toDomain(e));
        } finally {
            em.close();
        }
    }

    @Override
    public UHFReader update(UHFReader l) {
        if (l.getId() == null) throw new IllegalArgumentException("ID requerido para actualizar lector UHF");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            UHFReaderEntity e =
                    em.find(UHFReaderEntity.class, l.getId());
            if (e == null) throw new IllegalArgumentException("Lector UHF no encontrado con id " + l.getId());

            e.setCodigo(l.getCodigo());
            e.setDescripcion(l.getDescripcion());

            if (l.getUbicacion() == null || l.getUbicacion().getId() == null) {
                throw new IllegalArgumentException("Ubicación requerida para el lector UHF");
            }
            LocationEntity ubRef = em.getReference(LocationEntity.class, l.getUbicacion().getId());
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
    public Optional<UHFReader> findByCodigo(String codigo) {
        EntityManager em = emf.createEntityManager();
        try {
            List<UHFReaderEntity> list =
                    em.createQuery("select l from UHFReaderEntity l where l.codigo = :codigo",
                                    UHFReaderEntity.class)
                            .setParameter("codigo", codigo)
                            .setMaxResults(1)
                            .getResultList();
            return list.isEmpty() ? Optional.empty() : Optional.of(toDomain(list.get(0)));
        } finally {
            em.close();
        }
    }

    private UHFReader toDomain(UHFReaderEntity e) {
        LocationEntity ue = e.getUbicacion();
        Ubicacion u = (ue == null) ? null : new Ubicacion(ue.getId(), ue.getNombre());
        return new UHFReader(e.getId(), e.getCodigo(), e.getDescripcion(), u);
    }

    @Override
    public List<UHFReader> findByFilters(String codigo, Long ubicacionId) {
        EntityManager em = emf.createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("select l from UHFReaderEntity l where 1=1");

            if (codigo != null && !codigo.isBlank()) {
                jpql.append(" and lower(l.codigo) like lower(:codigo)");
            }
            if (ubicacionId != null) {
                jpql.append(" and l.ubicacion.id = :ubicacionId");
            }

            var query = em.createQuery(jpql.toString(), UHFReaderEntity.class);

            if (codigo != null && !codigo.isBlank()) {
                query.setParameter("codigo", "%" + codigo + "%");
            }
            if (ubicacionId != null) {
                query.setParameter("ubicacionId", ubicacionId);
            }

            List<UHFReaderEntity> list = query.getResultList();
            return list.stream().map(this::toDomain).collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public List<UHFReader> buscar(String codigo, Ubicacion ubicacion) {
        Long ubicacionId = (ubicacion != null) ? ubicacion.getId() : null;
        return findByFilters(codigo, ubicacionId);
    }
}