package domain.model;

public class TagUHF {

    private Long id;
    private String epc;
    private Tipo tipo;
    private boolean activo;

    public enum Tipo {
        EMPLEADO, EQUIPMENT
    }

    public TagUHF(Long id, String epc, Tipo tipo, boolean activo) {
        this.id = id;
        this.epc = epc;
        this.tipo = tipo;
        this.activo = activo;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}