
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
@Entity @Table(name="empleados")
public class EmpleadoEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(unique=true, length=64) private String codigo;
    @Column(name="full_name", nullable=false, length=150) private String fullName;
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getCodigo(){ return codigo; } public void setCodigo(String codigo){ this.codigo=codigo; }
    public String getFullName(){ return fullName; } public void setFullName(String fullName){ this.fullName=fullName; }
}
