package domain.model.tag;

import domain.model.UHFReader;
import domain.model.Ubicacion;

import java.time.LocalDateTime;

public class TagScan {
    private Long id;
    private UHFReader lector;
    private Ubicacion ubicacion;   // opcional
    private String epc;
    private Integer rssi;          // opcional
    private String machine;        // opcional
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public TagScan() {}

    public TagScan(Long id, UHFReader lector, Ubicacion ubicacion, String epc,
                   Integer rssi, String machine, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.lector = lector;
        this.ubicacion = ubicacion;
        this.epc = epc;
        this.rssi = rssi;
        this.machine = machine;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UHFReader getLector() { return lector; }
    public Ubicacion getUbicacion() { return ubicacion; }
    public String getEpc() { return epc; }
    public Integer getRssi() { return rssi; }
    public String getMachine() { return machine; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setLector(UHFReader lector) { this.lector = lector; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }
    public void setEpc(String epc) { this.epc = epc; }
    public void setRssi(Integer rssi) { this.rssi = rssi; }
    public void setMachine(String machine) { this.machine = machine; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}