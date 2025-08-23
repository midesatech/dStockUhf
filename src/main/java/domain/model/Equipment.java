
package domain.model;

public class Equipment {
    private Long id;
    private String sku;
    private String nombre;
    private Categoria categoria;
    private Ubicacion ubicacion;
    private String epc;

    public Equipment() {
    }

    public Equipment(Long id, String sku, String nombre) {
        this.id = id;
        this.sku = sku;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEpc() {
        return epc;
    }

    public void setEpc(String epc) {
        this.epc = epc;
    }

    @Override
    public String toString() {
        return sku.concat("-").concat(nombre);
    }

}
