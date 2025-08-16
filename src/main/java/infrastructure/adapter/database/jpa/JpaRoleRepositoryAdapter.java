
package infrastructure.adapter.database.jpa;
import jakarta.persistence.EntityManagerFactory; import jakarta.persistence.EntityManager; import jakarta.persistence.EntityTransaction;
import infrastructure.adapter.database.mysql.entity.RoleEntity;
import java.util.List;
import java.util.stream.Collectors;

public class JpaRoleRepositoryAdapter {
    private final EntityManagerFactory emf;
    public JpaRoleRepositoryAdapter(EntityManagerFactory emf){ this.emf = emf; }
    public long count(){ EntityManager em = emf.createEntityManager(); try{ return em.createQuery("select count(r) from RoleEntity r", Long.class).getSingleResult(); } finally { em.close(); } }
    public Long save(String name){ EntityManager em = emf.createEntityManager(); EntityTransaction tx = em.getTransaction(); try{ tx.begin(); RoleEntity r = new RoleEntity(); r.setName(name); em.persist(r); tx.commit(); return r.getId(); } catch(Exception ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally { em.close(); } }
    public void addPermission(Long roleId, String permName){ EntityManager em = emf.createEntityManager(); EntityTransaction tx = em.getTransaction(); try{ tx.begin(); RoleEntity r = em.find(RoleEntity.class, roleId); if(r==null) throw new IllegalArgumentException("Role not found"); List<infrastructure.adapter.database.mysql.entity.PermissionEntity> p = em.createQuery("select p from PermissionEntity p where p.name = :n", infrastructure.adapter.database.mysql.entity.PermissionEntity.class).setParameter("n", permName).getResultList(); if(p.isEmpty()){ infrastructure.adapter.database.mysql.entity.PermissionEntity pe = new infrastructure.adapter.database.mysql.entity.PermissionEntity(); pe.setName(permName); em.persist(pe); r.getPermissions().add(pe); } else { r.getPermissions().add(p.get(0)); } tx.commit(); } catch(Exception ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally { em.close(); } }
    public List<String> findAllNames(){ EntityManager em = emf.createEntityManager(); try{ return em.createQuery("select r.name from RoleEntity r", String.class).getResultList(); } finally { em.close(); } }
    public List<RoleEntity> findAll(){ EntityManager em = emf.createEntityManager(); try{ return em.createQuery("select r from RoleEntity r", RoleEntity.class).getResultList(); } finally { em.close(); } }
}
