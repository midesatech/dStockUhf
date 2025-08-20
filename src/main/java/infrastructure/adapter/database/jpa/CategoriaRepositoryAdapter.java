
package infrastructure.adapter.database.jpa;
import domain.gateway.CategoriaRepository;
import domain.model.Categoria;
import infrastructure.adapter.database.mysql.entity.CategoriaEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class CategoriaRepositoryAdapter implements CategoriaRepository {
    private final EntityManagerFactory emf;
    public CategoriaRepositoryAdapter(EntityManagerFactory emf){ this.emf = emf; }

    @Override
    public Categoria save(Categoria c) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CategoriaEntity e = new CategoriaEntity();
            e.setNombre(c.getNombre());
            em.persist(e);
            tx.commit();
            return new Categoria(e.getId(), e.getNombre());
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public List<Categoria> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<CategoriaEntity> list = em.createQuery("select c from CategoriaEntity c", CategoriaEntity.class)
                    .getResultList();
            return list.stream()
                    .map(e -> new Categoria(e.getId(), e.getNombre()))
                    .collect(Collectors.toList());
        } finally { em.close(); }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CategoriaEntity e = em.find(CategoriaEntity.class, id);
            if(e!=null) em.remove(e);
            tx.commit();
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            CategoriaEntity e = em.find(CategoriaEntity.class, id);
            return Optional.ofNullable(e == null ? null : new Categoria(e.getId(), e.getNombre()));
        } finally { em.close(); }
    }

    @Override
    public Categoria update(Categoria c) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CategoriaEntity e = em.find(CategoriaEntity.class, c.getId());
            if (e == null) {
                throw new IllegalArgumentException("Categoría no encontrada con id " + c.getId());
            }
            e.setNombre(c.getNombre());
            em.merge(e);
            tx.commit();
            return new Categoria(e.getId(), e.getNombre());
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
