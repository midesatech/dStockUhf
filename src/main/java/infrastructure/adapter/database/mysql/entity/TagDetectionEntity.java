package infrastructure.adapter.database.mysql.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "detecciones_tags")
public class TagDetectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lector_id", nullable = false)
    private UHFReaderEntity lector;

    @ManyToOne
    @JoinColumn(name = "tag_id", nullable = false)
    private UHFTagEntity tag;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    private Integer intensidad; // RSSI

    @Enumerated(EnumType.STRING)
    private Estado estado = Estado.OK;

    public enum Estado {
        OK, FUERA_DE_LUGAR
    }

    // Getters & Setters
}
