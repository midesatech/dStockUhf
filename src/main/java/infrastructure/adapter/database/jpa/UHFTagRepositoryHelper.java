package infrastructure.adapter.database.jpa;

import infrastructure.adapter.database.mysql.entity.UHFTagEntity;
import jakarta.persistence.EntityManager;

import java.util.List;

public class UHFTagRepositoryHelper {
    public static UHFTagEntity findOrCreateByEpc(EntityManager em, String epc, UHFTagEntity.Tipo tipo) {
        List<UHFTagEntity> list = em.createQuery(
                        "SELECT t FROM UHFTagEntity t WHERE t.epc = :epc", UHFTagEntity.class)
                .setParameter("epc", epc)
                .getResultList();

        if (!list.isEmpty()) {
            UHFTagEntity existing = list.get(0);
            if (existing.getTipo() != tipo) {
                throw new IllegalStateException(
                        "El EPC ya está registrado como " + existing.getTipo() + " y no puede asignarse a " + tipo);
            }
            if (!existing.isActivo()) {
                existing.setActivo(true);
            }
            return existing;
        }

        UHFTagEntity tag = new UHFTagEntity();
        tag.setEpc(epc);
        tag.setTipo(tipo);
        tag.setActivo(true);
        em.persist(tag);
        return tag;
    }
}
