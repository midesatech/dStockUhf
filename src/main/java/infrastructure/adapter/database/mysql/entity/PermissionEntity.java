
package infrastructure.adapter.database.mysql.entity;

import domain.model.Permission;
import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    // --- getters y setters ---
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // --- Conversiones ---
    public static PermissionEntity fromDomain(Permission permission) {
        PermissionEntity entity = new PermissionEntity();
        entity.setId(permission.getId());
        entity.setName(permission.getName());
        return entity;
    }

    public Permission toDomain() {
        return new Permission(this.id, this.name);
    }
}