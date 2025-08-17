
package infrastructure.adapter.database.memory;

import domain.gateway.UserRepository;
import domain.model.Role;
import domain.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryUserRepositoryAdapter implements UserRepository {
    private final Map<Long, User> data = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public InMemoryUserRepositoryAdapter() {
        User admin = new User(seq.getAndIncrement(), "ADMIN", "$2a$10$Mz0YG/R7GWLhbVAqS0mUp.ODJH0Demopefek12XKk5PA3QHt3dgzu", true);
        admin.getRoles().add(new Role(1L, "ADMINISTRADOR"));
        data.put(admin.getId(), admin);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return data.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) user.setId(seq.getAndIncrement());
        data.put(user.getId(), user);
        return user;
    }

    @Override
    public void deleteById(Long id) {
        User u = data.get(id);
        if (u != null && u.isSystemUser()) throw new IllegalStateException("No se puede eliminar el usuario ADMIN");
        data.remove(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public long count() {
        return data.size();
    }

    @Override
    public List<User> findAll() {
        return List.of();
    }
}
