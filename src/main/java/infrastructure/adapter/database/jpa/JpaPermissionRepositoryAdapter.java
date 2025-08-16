
package infrastructure.adapter.database.jpa;
import jakarta.persistence.EntityManagerFactory; import jakarta.persistence.EntityManager; import jakarta.persistence.EntityTransaction;
import infrastructure.adapter.database.mysql.entity.PermissionEntity;
import java.util.List;
import java.util.stream.Collectors;
public class JpaPermissionRepositoryAdapter {
    private final EntityManagerFactory emf; public JpaPermissionRepositoryAdapter(EntityManagerFactory emf){ this.emf = emf; }
    public long count(){ EntityManager em = emf.createEntityManager(); try{ return em.createQuery("select count(p) from PermissionEntity p", Long.class).getSingleResult(); } finally { em.close(); } }
    public Long save(String name){ EntityManager em = emf.createEntityManager(); EntityTransaction tx = em.getTransaction(); try{ tx.begin(); PermissionEntity p = new PermissionEntity(); p.setName(name); em.persist(p); tx.commit(); return p.getId(); } catch(Exception ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally { em.close(); } }
    public List<String> findAllNames(){ EntityManager em = emf.createEntityManager(); try{ return em.createQuery("select p.name from PermissionEntity p", String.class).getResultList(); } finally { em.close(); } }
    public List<PermissionEntity> findAll(){ EntityManager em = emf.createEntityManager(); try{ return em.createQuery("select p from PermissionEntity p", PermissionEntity.class).getResultList(); } finally { em.close(); } }
}
