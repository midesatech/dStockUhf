package domain.usecase;

import domain.gateway.PermissionRepository;
import domain.model.Permission;

import java.util.List;
import java.util.Optional;

public class PermissionUseCase {
    private final PermissionRepository repository;

    public PermissionUseCase(PermissionRepository repository) {
        this.repository = repository;
    }

    public long count() {
        return repository.count();
    }

    public Permission create(Permission permission) {
        return repository.save(permission);
    }

    public List<Permission> findAll() {
        return repository.findAll();
    }

    public Permission findByName(String name) {
        Optional<Permission> opt = repository.findByName(name);
        return opt.orElseThrow(() -> new IllegalArgumentException("Permission not found: " + name));
    }
}
