
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "categorias")
public class CategoriaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String nombre;
    public Long getId(){ return id; } public void setId(Long id){ this.id = id; }
    public String getNombre(){ return nombre; } public void setNombre(String nombre){ this.nombre = nombre; }
}
