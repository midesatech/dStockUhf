package infrastructure.adapter.database.mysql.entity.mapper;

import domain.model.Role;
import domain.model.Permission;
import infrastructure.adapter.database.mysql.entity.RoleEntity;
import infrastructure.adapter.database.mysql.entity.PermissionEntity;

import java.util.stream.Collectors;

public class RoleMapper {

    public static RoleEntity toEntity(Role role) {
        RoleEntity entity = new RoleEntity();
        entity.setId(role.getId());
        entity.setName(role.getName());
        entity.setPermissions(
                role.getPermissions().stream()
                        .map(RoleMapper::toPermissionEntity)
                        .collect(Collectors.toSet())
        );
        return entity;
    }

    public static Role toDomain(RoleEntity entity) {
        Role role = new Role(entity.getId(), entity.getName());
        role.setPermissions(
                entity.getPermissions().stream()
                        .map(RoleMapper::toPermissionDomain)
                        .collect(Collectors.toSet())
        );
        return role;
    }

    private static PermissionEntity toPermissionEntity(Permission permission) {
        PermissionEntity pe = new PermissionEntity();
        pe.setId(permission.getId());
        pe.setName(permission.getName());
        return pe;
    }

    private static Permission toPermissionDomain(PermissionEntity entity) {
        return new Permission(entity.getId(), entity.getName());
    }
}