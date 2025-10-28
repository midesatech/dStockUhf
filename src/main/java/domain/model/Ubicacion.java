
package domain.model;

public class Ubicacion {
    private Long id;
    private String nombre;
    private Long parentId;       // null = principal
    private String parentName;   // opcional para proyección/tabla

    public Ubicacion() {
    }

    public Ubicacion(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Ubicacion(Long id, String nombre, Long parentId, String parentName) {
        this.id = id; this.nombre = nombre; this.parentId = parentId; this.parentName = parentName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    @Override
    public String toString() {
        return parentName == null || parentId == null ? nombre : parentName + " / " + nombre;
    }
}
