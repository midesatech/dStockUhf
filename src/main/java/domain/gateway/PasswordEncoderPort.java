
package domain.gateway;

public interface PasswordEncoderPort {
    String encode(String raw);

    boolean matches(String raw, String hash);
}
