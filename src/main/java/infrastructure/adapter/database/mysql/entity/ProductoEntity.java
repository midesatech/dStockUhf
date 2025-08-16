
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
@Entity @Table(name="productos")
public class ProductoEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true, length=100) private String sku;
    @Column(nullable=false, length=200) private String nombre;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="categoria_id") private CategoriaEntity categoria;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="ubicacion_id") private UbicacionEntity ubicacion;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="empleado_id") private EmpleadoEntity empleado;
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getSku(){ return sku; } public void setSku(String sku){ this.sku=sku; }
    public String getNombre(){ return nombre; } public void setNombre(String nombre){ this.nombre=nombre; }
    public CategoriaEntity getCategoria(){ return categoria; } public void setCategoria(CategoriaEntity categoria){ this.categoria=categoria; }
    public UbicacionEntity getUbicacion(){ return ubicacion; } public void setUbicacion(UbicacionEntity ubicacion){ this.ubicacion=ubicacion; }
    public EmpleadoEntity getEmpleado(){ return empleado; } public void setEmpleado(EmpleadoEntity empleado){ this.empleado=empleado; }
}
