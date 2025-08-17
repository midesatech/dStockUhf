
package domain.usecase;

import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepository;
import domain.model.User;

public class ChangePasswordUseCase {
    private final UserRepository users;
    private final PasswordEncoderPort enc;

    public ChangePasswordUseCase(UserRepository users, PasswordEncoderPort enc) {
        this.users = users;
        this.enc = enc;
    }

    public void change(Long userId, String current, String next) {
        User u = users.findById(userId).orElseThrow();
        if (!enc.matches(current, u.getPasswordHash()))
            throw new IllegalArgumentException("Contraseña actual inválida");
        u.setPasswordHash(enc.encode(next));
        users.save(u);
    }
}
