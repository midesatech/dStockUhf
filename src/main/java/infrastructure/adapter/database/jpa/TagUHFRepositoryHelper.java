package infrastructure.adapter.database.jpa;

import infrastructure.adapter.database.mysql.entity.TagUHFEntity;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TagUHFRepositoryHelper {
    public static TagUHFEntity findOrCreateByEpc(EntityManager em, String epc, TagUHFEntity.Tipo tipo) {
        List<TagUHFEntity> list = em.createQuery(
                        "SELECT t FROM TagUHFEntity t WHERE t.epc = :epc", TagUHFEntity.class)
                .setParameter("epc", epc)
                .getResultList();

        if (!list.isEmpty()) {
            TagUHFEntity existing = list.get(0);
            if (existing.getTipo() != tipo) {
                throw new IllegalStateException(
                        "El EPC ya está registrado como " + existing.getTipo() + " y no puede asignarse a " + tipo);
            }
            if (!existing.isActivo()) {
                existing.setActivo(true);
            }
            return existing;
        }

        TagUHFEntity tag = new TagUHFEntity();
        tag.setEpc(epc);
        tag.setTipo(tipo);
        tag.setActivo(true);
        em.persist(tag);
        return tag;
    }
}
