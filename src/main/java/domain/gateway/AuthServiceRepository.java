
package domain.gateway;

import domain.model.User;

public interface AuthServiceRepository {
    User authenticate(String username, String rawPassword);
}
