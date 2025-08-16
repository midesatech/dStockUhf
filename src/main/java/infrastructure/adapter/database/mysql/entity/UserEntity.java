
package infrastructure.adapter.database.mysql.entity;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity @Table(name = "users")
public class UserEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true, length=64) private String username;
    @Column(name="password_hash", nullable=false, length=100) private String passwordHash;
    @Column(name="system_user", nullable=false) private boolean systemUser;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"), inverseJoinColumns=@JoinColumn(name="role_id"))
    private Set<RoleEntity> roles = new HashSet<>();
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getUsername(){ return username; } public void setUsername(String username){ this.username=username; }
    public String getPasswordHash(){ return passwordHash; } public void setPasswordHash(String passwordHash){ this.passwordHash=passwordHash; }
    public boolean isSystemUser(){ return systemUser; } public void setSystemUser(boolean systemUser){ this.systemUser=systemUser; }
    public Set<RoleEntity> getRoles(){ return roles; } public void setRoles(Set<RoleEntity> roles){ this.roles=roles; }
}
