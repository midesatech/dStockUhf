
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
@Entity @Table(name = "permissions")
public class PermissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=80) private String name;
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getName(){ return name; } public void setName(String name){ this.name=name; }
}
