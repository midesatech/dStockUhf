package domain.model;

import java.time.LocalDateTime;

public class DetectionRecord {
    private final String tipo;         // "EMPLOYEE" | "EQUIPMENT"
    private final String epc;
    private final String nombre;       // empleado/equipo si disponible
    private final Long   locationId;
    private final String locationName;
    private final LocalDateTime seenAt;

    public DetectionRecord(String tipo, String epc, String nombre, Long locationId, String locationName, LocalDateTime seenAt) {
        this.tipo = tipo;
        this.epc = epc;
        this.nombre = nombre;
        this.locationId = locationId;
        this.locationName = locationName;
        this.seenAt = seenAt;
    }

    public String getTipo() { return tipo; }
    public String getEpc() { return epc; }
    public String getNombre() { return nombre; }
    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public LocalDateTime getSeenAt() { return seenAt; }
}
