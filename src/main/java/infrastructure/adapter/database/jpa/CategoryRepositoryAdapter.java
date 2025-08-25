
package infrastructure.adapter.database.jpa;
import domain.gateway.CategoryRepository;
import domain.model.Category;
import infrastructure.adapter.database.mysql.entity.CategoryEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class CategoryRepositoryAdapter implements CategoryRepository {
    private final EntityManagerFactory emf;
    public CategoryRepositoryAdapter(EntityManagerFactory emf){ this.emf = emf; }

    @Override
    public Category save(Category c) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CategoryEntity e = new CategoryEntity();
            e.setNombre(c.getNombre());
            em.persist(e);
            tx.commit();
            return new Category(e.getId(), e.getNombre());
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public List<Category> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            List<CategoryEntity> list = em.createQuery("select c from CategoryEntity c", CategoryEntity.class)
                    .getResultList();
            return list.stream()
                    .map(e -> new Category(e.getId(), e.getNombre()))
                    .collect(Collectors.toList());
        } finally { em.close(); }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CategoryEntity e = em.find(CategoryEntity.class, id);
            if(e!=null) em.remove(e);
            tx.commit();
        } catch (RuntimeException ex){
            if(tx.isActive()) tx.rollback();
            throw ex;
        } finally { em.close(); }
    }

    @Override
    public Optional<Category> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            CategoryEntity e = em.find(CategoryEntity.class, id);
            return Optional.ofNullable(e == null ? null : new Category(e.getId(), e.getNombre()));
        } finally { em.close(); }
    }

    @Override
    public Category update(Category c) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CategoryEntity e = em.find(CategoryEntity.class, c.getId());
            if (e == null) {
                throw new IllegalArgumentException("Categoría no encontrada con id " + c.getId());
            }
            e.setNombre(c.getNombre());
            em.merge(e);
            tx.commit();
            return new Category(e.getId(), e.getNombre());
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
}
