package domain.model;

import java.time.LocalDateTime;


public class Occupant {
    private final String tipo; // "EMPLOYEE" | "EQUIPMENT"
    private final String epc;
    private final String nombre; // empleado: full_name + last_name; equipo: equipment.nombre
    private final LocalDateTime lastSeen;


    public Occupant(String tipo, String epc, String nombre, LocalDateTime lastSeen) {
        this.tipo = tipo;
        this.epc = epc;
        this.nombre = nombre;
        this.lastSeen = lastSeen;
    }
    public String getTipo() { return tipo; }
    public String getEpc() { return epc; }
    public String getNombre() { return nombre; }
    public LocalDateTime getLastSeen() { return lastSeen; }
}
