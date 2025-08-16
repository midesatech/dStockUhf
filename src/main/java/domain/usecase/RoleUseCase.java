package domain.usecase;

import domain.model.Role;
import domain.gateway.RoleRepository;

import java.util.List;
import java.util.Optional;

public class RoleUseCase {
    private final RoleRepository repository;

    public RoleUseCase(RoleRepository repository) {
        this.repository = repository;
    }

    public long count() {
        return repository.count();
    }

    public Role create(Role role) {
        return repository.save(role);
    }

    public List<Role> listAll() {
        return repository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<Role> findByName(String name) {
        return repository.findByName(name);
    }

    public void delete(Long id) {
        repository.delete(id);
    }
}
