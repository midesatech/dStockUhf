
package infrastructure.adapter.security;

import domain.gateway.AuthServicePort;
import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepositoryPort;
import domain.model.User;

public class SimpleAuthServiceAdapter implements AuthServicePort {
    private final UserRepositoryPort users;
    private final PasswordEncoderPort enc;

    public SimpleAuthServiceAdapter(UserRepositoryPort users, PasswordEncoderPort enc) {
        this.users = users;
        this.enc = enc;
    }

    @Override
    public User authenticate(String username, String raw) {
        return users.findByUsername(username)
                .filter(u -> enc.matches(raw, u.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
    }
}
