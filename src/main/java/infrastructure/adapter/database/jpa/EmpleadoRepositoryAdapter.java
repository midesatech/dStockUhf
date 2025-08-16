
package infrastructure.adapter.database.jpa;
import domain.gateway.EmpleadoGateway;
import domain.model.Empleado;
import infrastructure.adapter.database.mysql.entity.EmpleadoEntity;
import jakarta.persistence.EntityManagerFactory; import jakarta.persistence.EntityManager; import jakarta.persistence.EntityTransaction;
import java.util.List; import java.util.stream.Collectors; import java.util.Optional;
public class EmpleadoRepositoryAdapter implements EmpleadoGateway {
    private final EntityManagerFactory emf;
    public EmpleadoRepositoryAdapter(EntityManagerFactory emf){ this.emf=emf; }
    @Override public Empleado save(Empleado e){ EntityManager em=emf.createEntityManager(); EntityTransaction tx=em.getTransaction(); try{ tx.begin(); EmpleadoEntity en=new EmpleadoEntity(); en.setCodigo(e.getCodigo()); en.setFullName(e.getFullName()); em.persist(en); tx.commit(); return new Empleado(en.getId(), en.getCodigo(), en.getFullName()); }catch(Exception ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally{ em.close(); } }
    @Override public List<Empleado> findAll(){ EntityManager em=emf.createEntityManager(); try{ return em.createQuery("select e from EmpleadoEntity e", EmpleadoEntity.class).getResultList().stream().map(x->new Empleado(x.getId(), x.getCodigo(), x.getFullName())).collect(Collectors.toList()); } finally { em.close(); } }
    @Override public void deleteById(Long id){ EntityManager em=emf.createEntityManager(); EntityTransaction tx=em.getTransaction(); try{ tx.begin(); EmpleadoEntity e = em.find(EmpleadoEntity.class, id); if(e!=null) em.remove(e); tx.commit(); }catch(Exception ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally{ em.close(); } }
    @Override public Optional<Empleado> findById(Long id){ EntityManager em=emf.createEntityManager(); try{ EmpleadoEntity e = em.find(EmpleadoEntity.class, id); return Optional.ofNullable(e==null?null:new Empleado(e.getId(), e.getCodigo(), e.getFullName())); } finally{ em.close(); } }
}
