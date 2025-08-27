package domain.usecase.tag;

import domain.gateway.ScanRepository;
import domain.model.UHFReader;
import domain.model.Ubicacion;
import domain.model.tag.TagScan;
import domain.model.tag.TagScanFilter;

import java.util.List;

public class ScanUseCase {

    private final ScanRepository repo;

    public ScanUseCase(ScanRepository repo) {
        this.repo = repo;
    }

    /**
     * Registrar un scan venido de un lector/ESP32.
     * @param lectorCodigo Código del lector (UHFReaderEntity.codigo)
     * @param ubicacionId  Id ubicación (puede ser null: se infiere por el lector)
     * @param epc          EPC leído (hex string)
     * @param rssi         RSSI (opcional)
     * @param machine      Identificador máquina (opcional)
     */
    public TagScan recordScan(String lectorCodigo, Long ubicacionId, String epc, Integer rssi, String machine) {
        if (lectorCodigo == null || lectorCodigo.isBlank()) {
            throw new IllegalArgumentException("lectorCodigo requerido");
        }
        if (epc == null || epc.isBlank()) {
            throw new IllegalArgumentException("epc requerido");
        }

        TagScan scan = new TagScan();
        scan.setLector(new UHFReader(null, lectorCodigo.trim(), null, null));
        if (ubicacionId != null) {
            scan.setUbicacion(new Ubicacion(ubicacionId, null));
        }
        scan.setEpc(epc.trim());
        scan.setRssi(rssi);
        scan.setMachine(machine);

        return repo.save(scan);
    }

    public List<TagScan> buscar(TagScanFilter f) {
        return repo.findByFilters(f);
    }
}

