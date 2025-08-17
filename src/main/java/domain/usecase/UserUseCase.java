package domain.usecase;

import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepository;
import domain.model.User;

import java.util.List;
import java.util.Optional;

public class UserUseCase {
    private final UserRepository repository;
    private final PasswordEncoderPort encoder;

    public UserUseCase(UserRepository repository, PasswordEncoderPort encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public List<User> listAll() {
        // OJO: tu UserRepository no tenía findAll(), habría que implementarlo
        return repository.findAll();
    }

    public Optional<User> findById(Long id) {
        return repository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return repository.findByUsername(username);
    }

    public User save(User user, String rawPassword) {
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("El nombre de usuario es requerido");
        }

        if (user.getId() == null && (rawPassword == null || rawPassword.isBlank())) {
            throw new IllegalArgumentException("La contraseña es requerida para nuevos usuarios");
        }

        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPasswordHash(encoder.encode(rawPassword));
        }

        return repository.save(user);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public long count() {
        return repository.count();
    }

    public void changePassword(Long userId, String current, String next) {
        User u = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!encoder.matches(current, u.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña actual inválida");
        }

        u.setPasswordHash(encoder.encode(next));
        repository.save(u);
    }
}