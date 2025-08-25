
package domain.usecase;

import domain.gateway.AuthServiceRepository;
import domain.model.User;

public class AuthenticateUserUseCase {
    private final AuthServiceRepository auth;

    public AuthenticateUserUseCase(AuthServiceRepository auth) {
        this.auth = auth;
    }

    public User execute(String u, String p) {
        return auth.authenticate(u, p);
    }
}
