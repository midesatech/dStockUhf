package domain.gateway;

import domain.model.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {
    Permission save(Permission permission);
    List<Permission> findAll();
    Optional<Permission> findByName(String name);
    long count();

}

