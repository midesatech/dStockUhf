package infrastructure.adapter.database.jpa;

import domain.gateway.PeopleTypeRepository;
import domain.model.PeopleType;
import infrastructure.adapter.database.mysql.entity.PeopleTypeEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PeopleTypeRepositoryAdapter implements PeopleTypeRepository {
    private final EntityManagerFactory emf;

    public PeopleTypeRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public PeopleType save(PeopleType t) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PeopleTypeEntity e = toEntity(em, t);
            em.persist(e);
            tx.commit();
            return toDomain(e);
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public List<PeopleType> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM PeopleTypeEntity e ORDER BY e.nombre", PeopleTypeEntity.class)
                    .getResultList().stream().map(PeopleTypeRepositoryAdapter::toDomain).collect(Collectors.toList());
        } finally { em.close(); }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PeopleTypeEntity e = em.find(PeopleTypeEntity.class, id);
            if(e!=null) em.remove(e);
            tx.commit();
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public Optional<PeopleType> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(toDomain(em.find(PeopleTypeEntity.class, id)));
        } finally { em.close(); }
    }

    @Override
    public PeopleType update(PeopleType t) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            PeopleTypeEntity e = toEntity(em, t);
            e = em.merge(e);
            tx.commit();
            return toDomain(e);
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    private static PeopleType toDomain(PeopleTypeEntity e) {
        if(e==null) return null;
        return new PeopleType(e.getId(), e.getNombre());
    }

    private static PeopleTypeEntity toEntity(EntityManager em, PeopleType t) {
        PeopleTypeEntity e = (t.getId()!=null) ? em.find(PeopleTypeEntity.class, t.getId()) : new PeopleTypeEntity();
        e.setNombre(t.getNombre());
        return e;
    }
}
