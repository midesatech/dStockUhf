
package infrastructure.adapter.security;

import domain.gateway.PasswordEncoderPort;
import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoderAdapter implements PasswordEncoderPort {
    @Override
    public String encode(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt());
    }

    @Override
    public boolean matches(String raw, String hash) {
        return BCrypt.checkpw(raw, hash);
    }
}
