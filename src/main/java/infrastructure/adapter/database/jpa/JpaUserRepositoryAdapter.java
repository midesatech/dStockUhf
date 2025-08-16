
package infrastructure.adapter.database.jpa;
import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepositoryPort;
import domain.model.User;
import infrastructure.adapter.database.mysql.entity.UserEntity;
import jakarta.persistence.EntityManagerFactory; import jakarta.persistence.EntityManager; import jakarta.persistence.EntityTransaction;
import java.util.Optional;
public class JpaUserRepositoryAdapter implements UserRepositoryPort {
    private final EntityManagerFactory emf; private final PasswordEncoderPort encoder;
    public JpaUserRepositoryAdapter(EntityManagerFactory emf, PasswordEncoderPort encoder){ this.emf=emf; this.encoder=encoder; }
    @Override public Optional<User> findByUsername(String username){ EntityManager em = emf.createEntityManager(); try{ UserEntity ue = em.createQuery("select u from UserEntity u where upper(u.username)=upper(:u)", UserEntity.class).setParameter("u", username).getResultStream().findFirst().orElse(null); return Optional.ofNullable(EntityMappers.toDomain(ue)); } finally { em.close(); } }
    @Override public User save(User user){ EntityManager em = emf.createEntityManager(); EntityTransaction tx = em.getTransaction(); try{ tx.begin(); UserEntity e = EntityMappers.toEntity(user); UserEntity merged = em.merge(e); tx.commit(); user.setId(merged.getId()); return user; } catch(RuntimeException ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally { em.close(); } }
    @Override public void deleteById(Long id){ EntityManager em = emf.createEntityManager(); EntityTransaction tx = em.getTransaction(); try{ tx.begin(); UserEntity e = em.find(UserEntity.class, id); if(e!=null && e.isSystemUser()) throw new IllegalStateException("No se puede eliminar el usuario ADMIN"); if(e!=null) em.remove(e); tx.commit(); } catch(RuntimeException ex){ if(tx.isActive()) tx.rollback(); throw ex; } finally { em.close(); } }
    @Override public boolean existsByUsername(String username){ return findByUsername(username).isPresent(); }
    @Override public Optional<User> findById(Long id){ EntityManager em = emf.createEntityManager(); try{ UserEntity e = em.find(UserEntity.class, id); return Optional.ofNullable(EntityMappers.toDomain(e)); } finally { em.close(); } }
}
