
package domain.gateway;

import domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<User> findByUsername(String username);

    User save(User user);

    void deleteById(Long id);

    boolean existsByUsername(String username);

    java.util.Optional<User> findById(Long id);
}
