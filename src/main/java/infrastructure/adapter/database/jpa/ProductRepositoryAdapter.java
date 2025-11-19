
package infrastructure.adapter.database.jpa;

import domain.gateway.ProductGateway;
import domain.model.Category;
import domain.model.Product;
import domain.model.UHFTag;
import infrastructure.adapter.database.mysql.entity.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductRepositoryAdapter implements ProductGateway {
    private final EntityManagerFactory emf;

    public ProductRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Product save(Product product) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            ProductEntity e = (product.getId() != null)
                    ? em.find(ProductEntity.class, product.getId())
                    : new ProductEntity();

            e.setSku(product.getSku());
            e.setNombre(product.getNombre());

            if (product.getCategoria() != null) {
                CategoryEntity ce = em.find(CategoryEntity.class, product.getCategoria().getId());
                e.setCategoria(ce);
            } else {
                e.setCategoria(null);
            }

            if (product.getUbicacion() != null) {
                LocationEntity ue = em.find(LocationEntity.class, product.getUbicacion().getId());
                e.setUbicacion(ue);
            } else {
                e.setUbicacion(null);
            }

            // EPC -> TagUHFEntity (TIPO = EQUIPMENT)
            if (product.getEpc() != null && !product.getEpc().isBlank()) {
                UHFTagEntity tag = UHFTagRepositoryHelper.findOrCreateByEpc(
                        em, product.getEpc().trim(), UHFTag.Tipo.PRODUCT.toString()
                );
                e.setTag(tag);
            } else {
                e.setTag(null);
            }

            if (e.getId() == null) {
                em.persist(e);
            } else {
                e = em.merge(e);
            }

            tx.commit();
            return toDomain(e);
        } catch (RuntimeException ex) {
            if (tx.isActive()) tx.rollback();
            throw ex;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM ProductEntity p", ProductEntity.class)
                    .getResultList()
                    .stream()
                    .map(ProductRepositoryAdapter::toDomain)
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
            ProductEntity e = em.find(ProductEntity.class, id);
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
    public Optional<Product> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            ProductEntity e = em.find(ProductEntity.class, id);
            return Optional.ofNullable(e == null ? null : toDomain(e));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> buscar(String sku, String nombre, Category cat) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM ProductEntity e WHERE 1=1";
            if (sku != null && !sku.isBlank()) jpql += " AND e.sku LIKE :sku";
            if (nombre != null && !nombre.isBlank()) jpql += " AND e.nombre LIKE :nombre";
            if (cat != null) jpql += " AND e.categoria.id = :catId";

            var q = em.createQuery(jpql, ProductEntity.class);
            if (sku != null && !sku.isBlank()) q.setParameter("sku", "%" + sku + "%");
            if (nombre != null && !nombre.isBlank()) q.setParameter("nombre", "%" + nombre + "%");
            if (cat != null) q.setParameter("catId", cat.getId());

            return q.getResultList().stream()
                    .map(ProductRepositoryAdapter::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Product> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM ProductEntity e " +
                            "JOIN e.tag t " +
                            "WHERE t.epc = :epc",
                    ProductEntity.class);
            query.setParameter("epc", epc);
            List<ProductEntity> result = query.getResultList();
            return query.getResultStream()
                    .findFirst()
                    .map(ProductRepositoryAdapter::toDomain);
        } finally {
            em.close();
        }
    }

    private static Product toDomain(ProductEntity e) {
        Product p = new Product(e.getId(), e.getSku(), e.getNombre());
        if (e.getCategoria() != null)
            p.setCategoria(new Category(e.getCategoria().getId(), e.getCategoria().getNombre()));
        if (e.getUbicacion() != null)
            p.setUbicacion(new domain.model.Ubicacion(e.getUbicacion().getId(), e.getUbicacion().getNombre()));
        if (e.getTag() != null)
            p.setEpc(e.getTag().getEpc());
        return p;
    }

}