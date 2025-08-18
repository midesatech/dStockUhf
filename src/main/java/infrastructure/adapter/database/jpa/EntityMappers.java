
package infrastructure.adapter.database.jpa;

import domain.model.Permission;
import domain.model.Role;
import domain.model.User;
import infrastructure.adapter.database.mysql.entity.PermissionEntity;
import infrastructure.adapter.database.mysql.entity.RoleEntity;
import infrastructure.adapter.database.mysql.entity.UserEntity;

import java.util.stream.Collectors;

public class EntityMappers {

    // ========== USER ==========
    public static User toDomain(UserEntity e) {
        if (e == null) return null;
        User d = new User(e.getId(), e.getUsername(), e.getPasswordHash(), e.isSystemUser());

        if (e.getRoles() != null) {
            d.setRoles(
                    e.getRoles().stream()
                            .map(EntityMappers::toDomain) // 👈 carga permisos
                            .collect(Collectors.toSet())
            );
        }
        return d;
    }

    // Usuario ligero (sin passwordHash, roles sin permisos)
    public static User toDomainLight(UserEntity e) {
        if (e == null) return null;

        User d = new User(
                e.getId(),
                e.getUsername(),
                null,              // 🚫 nunca exponemos passwordHash
                e.isSystemUser()
        );

        if (e.getRoles() != null) {
            d.setRoles(
                    e.getRoles().stream()
                            .map(EntityMappers::toDomainShallow) // ✅ solo id + name
                            .collect(Collectors.toSet())
            );
        }

        return d;
    }


    public static User toDomainWithPermissions(UserEntity e) {
        if (e == null) return null;
        User d = new User(e.getId(), e.getUsername(), e.getPasswordHash(), e.isSystemUser());

        if (e.getRoles() != null) {
            d.setRoles(e.getRoles().stream()
                    .map(EntityMappers::toDomain) // 👈 este sí carga permisos
                    .collect(Collectors.toSet()));
        }
        return d;
    }

    public static UserEntity toEntity(User d) {
        if (d == null) return null;

        UserEntity e = new UserEntity();
        e.setId(d.getId());
        e.setUsername(d.getUsername());
        e.setPasswordHash(d.getPasswordHash());
        e.setSystemUser(d.isSystemUser());

        if (d.getRoles() != null && !d.getRoles().isEmpty()) {
            e.setRoles(
                    d.getRoles().stream()
                            .map(r -> {
                                RoleEntity re = new RoleEntity();
                                re.setId(r.getId()); // 👈 solo ID, JPA resolverá la relación
                                return re;
                            })
                            .collect(Collectors.toSet())
            );
        }

        return e;
    }

    // Rol completo con permisos
    public static Role toDomain(RoleEntity e) {
        if (e == null) return null;
        Role r = new Role(e.getId(), e.getName());
        if (e.getPermissions() != null) {
            r.setPermissions(e.getPermissions().stream()
                    .map(EntityMappers::toDomain)
                    .collect(Collectors.toSet()));
        }
        return r;
    }

    public static RoleEntity toEntity(Role d) {
        if (d == null) return null;
        RoleEntity e = new RoleEntity();
        e.setId(d.getId());
        e.setName(d.getName());
        if (d.getPermissions() != null) {
            e.setPermissions(d.getPermissions().stream()
                    .map(EntityMappers::toEntity)
                    .collect(Collectors.toSet()));
        }
        return e;
    }

    // Rol ligero (solo id + name, sin permisos)
    public static Role toDomainShallow(RoleEntity e) {
        if (e == null) return null;
        return new Role(e.getId(), e.getName());
    }

    // ========== PERMISSION ==========
    public static Permission toDomain(PermissionEntity e) {
        if (e == null) return null;
        return new Permission(e.getId(), e.getName());
    }

    public static PermissionEntity toEntity(Permission d) {
        if (d == null) return null;
        PermissionEntity e = new PermissionEntity();
        e.setId(d.getId());
        e.setName(d.getName());
        return e;
    }
}