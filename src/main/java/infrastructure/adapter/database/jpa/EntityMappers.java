
package infrastructure.adapter.database.jpa;
import domain.model.Permission; import domain.model.Role; import domain.model.User;
import infrastructure.adapter.database.mysql.entity.PermissionEntity; import infrastructure.adapter.database.mysql.entity.RoleEntity; import infrastructure.adapter.database.mysql.entity.UserEntity;
import java.util.stream.Collectors;
public class EntityMappers {
    public static User toDomain(UserEntity e){ if(e==null) return null; User d = new User(e.getId(), e.getUsername(), e.getPasswordHash(), e.isSystemUser()); d.setRoles(e.getRoles().stream().map(EntityMappers::toDomain).collect(Collectors.toSet())); return d; }
    public static Role toDomain(RoleEntity e){ Role r = new Role(e.getId(), e.getName()); r.setPermissions(e.getPermissions().stream().map(EntityMappers::toDomain).collect(Collectors.toSet())); return r; }
    public static Permission toDomain(PermissionEntity e){ return new Permission(e.getId(), e.getName()); }
    public static UserEntity toEntity(User d){ UserEntity e = new UserEntity(); e.setId(d.getId()); e.setUsername(d.getUsername()); e.setPasswordHash(d.getPasswordHash()); e.setSystemUser(d.isSystemUser()); return e; }
}
