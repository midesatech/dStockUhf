package infrastructure.adapter.database.jpa;

import domain.gateway.TagUHFRepository;
import domain.model.UHFTag;
import infrastructure.adapter.database.mysql.entity.UHFTagEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UHFTagRepositoryAdapter implements TagUHFRepository {

    private final EntityManagerFactory emf;

    public UHFTagRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public UHFTag save(UHFTag tag) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            UHFTagEntity e = new UHFTagEntity();
            e.setEpc(tag.getEpc());
            e.setTipo(UHFTagEntity.Tipo.valueOf(tag.getTipo().name()));
            e.setActivo(tag.isActivo());
            em.persist(e);
            tx.commit();
            return new UHFTag(e.getId(), e.getEpc(), UHFTag.Tipo.valueOf(e.getTipo().name()), e.isActivo());
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<UHFTag> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<UHFTagEntity> list = em.createQuery("select t from UHFTagEntity t", UHFTagEntity.class)
                    .getResultList();
            return list.stream()
                    .map(e -> new UHFTag(e.getId(), e.getEpc(), UHFTag.Tipo.valueOf(e.getTipo().name()), e.isActivo()))
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
            UHFTagEntity e = em.find(UHFTagEntity.class, id);
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
    public Optional<UHFTag> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            UHFTagEntity e = em.find(UHFTagEntity.class, id);
            return Optional.ofNullable(e == null ? null :
                    new UHFTag(e.getId(), e.getEpc(), UHFTag.Tipo.valueOf(e.getTipo().name()), e.isActivo()));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<UHFTag> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM UHFTagEntity e WHERE e.epc = :epc",
                    UHFTagEntity.class);
            query.setParameter("epc", epc);
            List<UHFTagEntity> result = query.getResultList();
            return result.isEmpty() ? Optional.empty() :
                    Optional.of(
                    new UHFTag(result.get(0).getId(), result.get(0).getEpc(), UHFTag.Tipo.valueOf(result.get(0).getTipo().name()), result.get(0).isActivo()));
        } finally {
            em.close();
        }
    }

    @Override
    public UHFTag update(UHFTag tag) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            UHFTagEntity e = em.find(UHFTagEntity.class, tag.getId());
            if (e == null) {
                throw new IllegalArgumentException("Tag no encontrado con id " + tag.getId());
            }
            e.setEpc(tag.getEpc());
            e.setTipo(UHFTagEntity.Tipo.valueOf(tag.getTipo().name()));
            e.setActivo(tag.isActivo());
            em.merge(e);
            tx.commit();
            return new UHFTag(e.getId(), e.getEpc(), UHFTag.Tipo.valueOf(e.getTipo().name()), e.isActivo());
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}