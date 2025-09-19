package infrastructure.adapter.database.mysql.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "detecciones_tags",
        indexes = {
                @Index(name = "idx_detecciones_epc", columnList = "epc"),
                @Index(name = "idx_detecciones_created", columnList = "created_at"),
                @Index(name = "idx_detecciones_lector_created", columnList = "lector_id, created_at")
        }
)
public class TagDetectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Lector que reportó la detección (obligatorio)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "lector_id", nullable = false)
    private UHFReaderEntity lector;

    // Ubicación (opcional). Si no se envía, se puede inferir desde el lector.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private LocationEntity ubicacion;

    @Column(name = "epc", nullable = false, length = 64)
    private String epc;

    @Column(name = "rssi")
    private Integer rssi;

    @Column(name = "machine", length = 100)
    private String machine;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- lifecycle ---
    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- getters/setters ---
    public Long getId() { return id; }
    public UHFReaderEntity getLector() { return lector; }
    public void setLector(UHFReaderEntity lector) { this.lector = lector; }
    public LocationEntity getUbicacion() { return ubicacion; }
    public void setUbicacion(LocationEntity ubicacion) { this.ubicacion = ubicacion; }
    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }
    public Integer getRssi() { return rssi; }
    public void setRssi(Integer rssi) { this.rssi = rssi; }
    public String getMachine() { return machine; }
    public void setMachine(String machine) { this.machine = machine; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
