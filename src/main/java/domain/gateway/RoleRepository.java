package domain.gateway;

import domain.model.Role;

import java.util.List;
import java.util.Optional;

public interface RoleRepository {
    List<Role> findAll();
    Optional<Role> findById(Long id);
    Role save(Role role);
    void delete(Long id);
    Optional<Role> findByName(String name);
    long count();
}
