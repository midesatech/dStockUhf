package domain.gateway;

import domain.model.tag.TagScan;
import domain.model.tag.TagScanFilter;

import java.util.List;

public interface ScanRepository {
    TagScan save(TagScan scan);

    /**
     * Búsqueda flexible con filtros (cualquiera puede ser null/blank).
     * Orden: más recientes primero.
     */
    List<TagScan> findByFilters(TagScanFilter f);
}
