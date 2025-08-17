
package domain.usecase;

import domain.gateway.AuthServicePort;
import domain.model.User;

public class AuthenticateUserUseCase {
    private final AuthServicePort auth;

    public AuthenticateUserUseCase(AuthServicePort auth) {
        this.auth = auth;
    }

    public User execute(String u, String p) {
        return auth.authenticate(u, p);
    }
}
