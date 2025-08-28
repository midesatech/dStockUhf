package infrastructure.fx.controller.dashboard;

public class OccupantRow {
    private String tipo;
    private String epc;
    private String nombre; // opcional, si luego haces JOIN con empleados/equipos
    private String ultimo;


    public OccupantRow(String tipo, String epc, String nombre, String ultimo) {
        this.tipo = tipo; this.epc = epc; this.nombre = nombre; this.ultimo = ultimo;
    }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getEpc() { return epc; }
    public void setEpc(String epc) { this.epc = epc; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getUltimo() { return ultimo; }
    public void setUltimo(String ultimo) { this.ultimo = ultimo; }
}
