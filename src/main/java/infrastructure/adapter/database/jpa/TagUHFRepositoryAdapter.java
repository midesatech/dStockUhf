package infrastructure.adapter.database.jpa;

import domain.gateway.TagUHFRepository;
import domain.model.Equipment;
import domain.model.TagUHF;
import infrastructure.adapter.database.mysql.entity.EquipmentEntity;
import infrastructure.adapter.database.mysql.entity.TagUHFEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TagUHFRepositoryAdapter implements TagUHFRepository {

    private final EntityManagerFactory emf;

    public TagUHFRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public TagUHF save(TagUHF tag) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TagUHFEntity e = new TagUHFEntity();
            e.setEpc(tag.getEpc());
            e.setTipo(TagUHFEntity.Tipo.valueOf(tag.getTipo().name()));
            e.setActivo(tag.isActivo());
            em.persist(e);
            tx.commit();
            return new TagUHF(e.getId(), e.getEpc(), TagUHF.Tipo.valueOf(e.getTipo().name()), e.isActivo());
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<TagUHF> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<TagUHFEntity> list = em.createQuery("select t from TagUHFEntity t", TagUHFEntity.class)
                    .getResultList();
            return list.stream()
                    .map(e -> new TagUHF(e.getId(), e.getEpc(), TagUHF.Tipo.valueOf(e.getTipo().name()), e.isActivo()))
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
            TagUHFEntity e = em.find(TagUHFEntity.class, id);
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
    public Optional<TagUHF> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            TagUHFEntity e = em.find(TagUHFEntity.class, id);
            return Optional.ofNullable(e == null ? null :
                    new TagUHF(e.getId(), e.getEpc(), TagUHF.Tipo.valueOf(e.getTipo().name()), e.isActivo()));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<TagUHF> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM TagUHFEntity e WHERE e.epc = :epc",
                    TagUHFEntity.class);
            query.setParameter("epc", epc);
            List<TagUHFEntity> result = query.getResultList();
            return result.isEmpty() ? Optional.empty() :
                    Optional.of(
                    new TagUHF(result.get(0).getId(), result.get(0).getEpc(), TagUHF.Tipo.valueOf(result.get(0).getTipo().name()), result.get(0).isActivo()));
        } finally {
            em.close();
        }
    }

    @Override
    public TagUHF update(TagUHF tag) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TagUHFEntity e = em.find(TagUHFEntity.class, tag.getId());
            if (e == null) {
                throw new IllegalArgumentException("Tag no encontrado con id " + tag.getId());
            }
            e.setEpc(tag.getEpc());
            e.setTipo(TagUHFEntity.Tipo.valueOf(tag.getTipo().name()));
            e.setActivo(tag.isActivo());
            em.merge(e);
            tx.commit();
            return new TagUHF(e.getId(), e.getEpc(), TagUHF.Tipo.valueOf(e.getTipo().name()), e.isActivo());
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}