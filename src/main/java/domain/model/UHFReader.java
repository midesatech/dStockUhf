package domain.model;

public class UHFReader {
    private Long id;
    private String codigo;
    private String descripcion;
    private Ubicacion ubicacion; // ya existe en tu dominio

    public UHFReader() {
    }

    public UHFReader(String codigo, String descripcion, Ubicacion ubicacion) {
        this(null, codigo, descripcion, ubicacion);
    }

    public UHFReader(Long id, String codigo, String descripcion, Ubicacion ubicacion) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }
}

