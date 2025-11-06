package domain.model;

public class UHFTag {

    private Long id;
    private String epc;
    private Tipo tipo;
    private boolean activo;

    public enum Tipo {
        EMPLOYEE("EMPLEADO", true),
        EQUIPMENT("EQUIPO", false),
        PRODUCT("PRODUCTO", true);

        private final String label;
        private final boolean isEnabled;

        // Constructor that accepts the label and the status
        Tipo(String label, boolean isEnabled) {
            this.label = label;
            this.isEnabled = isEnabled;
        }


        // Getter methods
        public String getLabel() {
            return label;
        }

        public boolean isEnabled() {
            return isEnabled;
        }
    }

    public UHFTag(Long id, String epc, Tipo tipo, boolean activo) {
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