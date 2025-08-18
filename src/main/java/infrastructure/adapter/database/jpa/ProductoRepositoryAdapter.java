
package infrastructure.adapter.database.jpa;

import domain.gateway.ProductoGateway;
import domain.model.Producto;
import infrastructure.adapter.database.mysql.entity.ProductoEntity;
import infrastructure.adapter.database.mysql.entity.CategoriaEntity;
import infrastructure.adapter.database.mysql.entity.UbicacionEntity;
import infrastructure.adapter.database.mysql.entity.EmpleadoEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class ProductoRepositoryAdapter implements ProductoGateway {
    private final EntityManagerFactory emf;

    public ProductoRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Producto save(Producto p) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ProductoEntity e = new ProductoEntity();
            e.setSku(p.getSku());
            e.setNombre(p.getNombre());
            if (p.getCategoria() != null) {
                CategoriaEntity ce = em.find(CategoriaEntity.class, p.getCategoria().getId());
                e.setCategoria(ce);
            }
            if (p.getUbicacion() != null) {
                UbicacionEntity ue = em.find(UbicacionEntity.class, p.getUbicacion().getId());
                e.setUbicacion(ue);
            }
            if (p.getResponsable() != null) {
                EmpleadoEntity re = em.find(EmpleadoEntity.class, p.getResponsable().getId());
                e.setEmpleado(re);
            }
            em.persist(e);
            tx.commit();
            return toDomain(e);
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Producto> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select p from ProductoEntity p", ProductoEntity.class).getResultList().stream().map(ProductoRepositoryAdapter::toDomain).collect(Collectors.toList());
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
            ProductoEntity e = em.find(ProductoEntity.class, id);
            if (e != null) em.remove(e);
            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Producto> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductoEntity e = em.find(ProductoEntity.class, id);
            return Optional.ofNullable(e == null ? null : toDomain(e));
        } finally {
            em.close();
        }
    }

    private static Producto toDomain(ProductoEntity e) {
        Producto p = new Producto(e.getId(), e.getSku(), e.getNombre());
        if (e.getCategoria() != null)
            p.setCategoria(new domain.model.Categoria(e.getCategoria().getId(), e.getCategoria().getNombre()));
        if (e.getUbicacion() != null)
            p.setUbicacion(new domain.model.Ubicacion(e.getUbicacion().getId(), e.getUbicacion().getNombre()));
        if (e.getEmpleado() != null)
            p.setResponsable(new domain.model.Empleado(e.getEmpleado().getId(), e.getEmpleado().getCodigo(), e.getEmpleado().getFullName(), e.getEmpleado().getLastName()));
        return p;
    }
}
