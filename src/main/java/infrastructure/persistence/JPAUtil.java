
package infrastructure.persistence;

import app.config.DbConfig;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

public class JPAUtil {
    private static EntityManagerFactory emf;

    public static void init() {
        if (emf == null) {
            // Allow overrides from environment (e.g., JDBC_URL, DB_HOST, etc.)
            Map<String, Object> overrides = DbConfig.jpaOverrides();
            emf = Persistence.createEntityManagerFactory("InventarioPU", overrides);
        }
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public static void shutdown() {
        if (emf != null) {
            emf.close();
            emf = null;
        }
    }
}
