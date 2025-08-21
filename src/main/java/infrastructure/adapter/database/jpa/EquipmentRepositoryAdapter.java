
package infrastructure.adapter.database.jpa;

import domain.gateway.EquipmentGateway;
import domain.model.Categoria;
import domain.model.Equipment;
import infrastructure.adapter.database.mysql.entity.EquipmentEntity;
import infrastructure.adapter.database.mysql.entity.CategoriaEntity;
import infrastructure.adapter.database.mysql.entity.UbicacionEntity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

public class EquipmentRepositoryAdapter implements EquipmentGateway {
    private final EntityManagerFactory emf;

    public EquipmentRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Equipment save(Equipment p) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            EquipmentEntity e = new EquipmentEntity();
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
    public List<Equipment> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("select p from EquipmentEntity p", EquipmentEntity.class)
                    .getResultList()
                    .stream()
                    .map(EquipmentRepositoryAdapter::toDomain)
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
            EquipmentEntity e = em.find(EquipmentEntity.class, id);
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
    public Optional<Equipment> findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            EquipmentEntity e = em.find(EquipmentEntity.class, id);
            return Optional.ofNullable(e == null ? null : toDomain(e));
        } finally {
            em.close();
        }
    }

    private static Equipment toDomain(EquipmentEntity e) {
        Equipment p = new Equipment(e.getId(), e.getSku(), e.getNombre());
        if (e.getCategoria() != null)
            p.setCategoria(new domain.model.Categoria(e.getCategoria().getId(), e.getCategoria().getNombre()));
        if (e.getUbicacion() != null)
            p.setUbicacion(new domain.model.Ubicacion(e.getUbicacion().getId(), e.getUbicacion().getNombre()));
        return p;
    }

    @Override
    public List<Equipment> buscar(String sku, String nombre, Categoria cat) {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM EquipmentEntity e WHERE 1=1";
            if (sku != null && !sku.isBlank()) jpql += " AND e.sku LIKE :sku";
            if (nombre != null && !nombre.isBlank()) jpql += " AND e.nombre LIKE :nombre";
            if (cat != null) jpql += " AND e.categoria.id = :catId";

            var q = em.createQuery(jpql, EquipmentEntity.class);

            if (sku != null && !sku.isBlank()) q.setParameter("sku", "%" + sku + "%");
            if (nombre != null && !nombre.isBlank()) q.setParameter("nombre", "%" + nombre + "%");
            if (cat != null) q.setParameter("catId", cat.getId());

            return q.getResultList()
                    .stream()
                    .map(EquipmentRepositoryAdapter::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }
}
