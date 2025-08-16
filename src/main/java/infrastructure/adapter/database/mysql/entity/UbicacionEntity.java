
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
@Entity @Table(name="ubicaciones")
public class UbicacionEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=150) private String nombre;
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getNombre(){ return nombre; } public void setNombre(String nombre){ this.nombre=nombre; }
}
