
package infrastructure.adapter.database.jpa;

import domain.gateway.EquipmentGateway;
import domain.model.Equipment;
import infrastructure.adapter.database.mysql.entity.*;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EquipmentRepositoryAdapter implements EquipmentGateway {
    private final EntityManagerFactory emf;

    public EquipmentRepositoryAdapter(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Equipment save(Equipment equipment) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            EquipmentEntity e = (equipment.getId() != null)
                    ? em.find(EquipmentEntity.class, equipment.getId())
                    : new EquipmentEntity();

            e.setSku(equipment.getSku());
            e.setNombre(equipment.getNombre());

            if (equipment.getCategoria() != null) {
                CategoriaEntity ce = em.find(CategoriaEntity.class, equipment.getCategoria().getId());
                e.setCategoria(ce);
            } else {
                e.setCategoria(null);
            }

            if (equipment.getUbicacion() != null) {
                UbicacionEntity ue = em.find(UbicacionEntity.class, equipment.getUbicacion().getId());
                e.setUbicacion(ue);
            } else {
                e.setUbicacion(null);
            }

            // EPC -> TagUHFEntity (TIPO = EQUIPMENT)
            if (equipment.getEpc() != null && !equipment.getEpc().isBlank()) {
                TagUHFEntity tag = TagUHFRepositoryHelper.findOrCreateByEpc(
                        em, equipment.getEpc().trim(), TagUHFEntity.Tipo.EQUIPMENT
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
    public List<Equipment> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM EquipmentEntity p", EquipmentEntity.class)
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
        } catch (RuntimeException ex) {
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

    @Override
    public List<Equipment> buscar(String sku, String nombre, domain.model.Categoria cat) {
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

            return q.getResultList().stream()
                    .map(EquipmentRepositoryAdapter::toDomain)
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Equipment> findByEpc(String epc) {
        EntityManager em = emf.createEntityManager();
        try {
            var query = em.createQuery(
                    "SELECT e FROM EquipmentEntity e " +
                            "JOIN e.tag t " +
                            "WHERE t.epc = :epc",
                    EquipmentEntity.class);
            query.setParameter("epc", epc);
            List<EquipmentEntity> result = query.getResultList();
            return query.getResultStream()
                    .findFirst()
                    .map(EquipmentRepositoryAdapter::toDomain);
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
        if (e.getTag() != null)
            p.setEpc(e.getTag().getEpc());
        return p;
    }

}