package domain.model.tag;

import java.time.LocalDateTime;

public class TagScanFilter {
    public String epc;
    public String lectorCodigo;
    public Long ubicacionId;        // opcional
    public LocalDateTime desde;     // opcional
    public LocalDateTime hasta;     // opcional
    public Integer rssiMin;         // opcional
    public String machine;          // opcional
    public int limit = 500;         // por defecto

    public TagScanFilter withLimit(int l) { this.limit = l; return this; }
}
