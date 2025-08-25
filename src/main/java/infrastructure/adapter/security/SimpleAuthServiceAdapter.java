
package infrastructure.adapter.security;

import domain.gateway.AuthServiceRepository;
import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepository;
import domain.model.User;

public class SimpleAuthServiceAdapter implements AuthServiceRepository {
    private final UserRepository users;
    private final PasswordEncoderPort enc;

    public SimpleAuthServiceAdapter(UserRepository users, PasswordEncoderPort enc) {
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
