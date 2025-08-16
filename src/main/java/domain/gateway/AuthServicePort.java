
package domain.gateway; import domain.model.User; public interface AuthServicePort { User authenticate(String username, String rawPassword); }
