
package infrastructure.persistence;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {
    private static EntityManagerFactory emf;

    public static void init() {
        if (emf == null) emf = Persistence.createEntityManagerFactory("InventarioPU");
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
