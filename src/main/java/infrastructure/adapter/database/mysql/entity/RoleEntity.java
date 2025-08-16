
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
import java.util.HashSet; import java.util.Set;
@Entity @Table(name = "roles")
public class RoleEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=50) private String name;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="roles_permissions", joinColumns=@JoinColumn(name="role_id"), inverseJoinColumns=@JoinColumn(name="permission_id"))
    private Set<PermissionEntity> permissions = new HashSet<>();
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getName(){ return name; } public void setName(String name){ this.name=name; }
    public Set<PermissionEntity> getPermissions(){ return permissions; } public void setPermissions(Set<PermissionEntity> permissions){ this.permissions=permissions; }
}
