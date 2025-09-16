package app.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class DbConfig {
    private DbConfig() {
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v != null && !v.isEmpty()) ? v : def;
    }

    public static String profile() {
        return env("PROFILE", "dev").toLowerCase();
    }

    public static String username() {
        return env("DB_USER", "root");
    }

    public static String password() {
        return env("DB_PASS", "");
    }

    public static String host() {
        return env("DB_HOST", "localhost");
    }

    public static String port() {
        return env("DB_PORT", "3306");
    }

    public static String dbName() {
        return env("DB_NAME", "inventario");
    }

    public static String jdbcUrl() {
        String jdbc = System.getenv("JDBC_URL");
        if (jdbc != null && !jdbc.isEmpty()) {
            return jdbc;
        }
        return String.format(
                "jdbc:mariadb://%s:%s/%s",
                env("DB_HOST", "localhost"),
                env("DB_PORT", "3306"),
                env("DB_NAME", "inventario")
        );
    }

    public static Map<String, Object> jpaOverrides() {
        Map<String, Object> map = new HashMap<>();

        // 1) Load from properties file if present
        String fileHost = PropertyConfigService.get(PropertyConfigService.KEY_DB_HOST, "");
        String filePort = PropertyConfigService.get(PropertyConfigService.KEY_DB_PORT, "");
        String fileUser = PropertyConfigService.get(PropertyConfigService.KEY_DB_USER, "");
        String filePass = PropertyConfigService.get(PropertyConfigService.KEY_DB_PASSWORD, "");

        // 2) Allow env to override properties (if provided)
        String host = Optional.ofNullable(System.getenv("DB_HOST"))
                .filter(s -> !s.isEmpty()).orElse(!fileHost.isEmpty() ? fileHost : "localhost");

        String port = Optional.ofNullable(System.getenv("DB_PORT"))
                .filter(s -> !s.isEmpty()).orElse(!filePort.isEmpty() ? filePort : "3306");

        String user = Optional.ofNullable(System.getenv("DB_USER"))
                .filter(s -> !s.isEmpty()).orElse(!fileUser.isEmpty() ? fileUser : "root");

        String pass = Optional.ofNullable(System.getenv("DB_PASS"))
                .orElse(filePass); // can be empty

        // NOTE: database name from persistence.xml (inventario)
        String url = Optional.ofNullable(System.getenv("JDBC_URL"))
                .filter(s -> !s.isEmpty())
                .orElse("jdbc:mariadb://" + host + ":" + port + "/inventario");

        map.put("jakarta.persistence.jdbc.url", url);
        map.put("jakarta.persistence.jdbc.user", user);
        map.put("jakarta.persistence.jdbc.password", pass);

        // Hibernate settings (same logic you already had)
        map.put("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect");
        // Profile-specific safe defaults
        if ("prod".equals(profile())) {
            map.put("hibernate.show_sql", "false");
            map.put("hibernate.hbm2ddl.auto", Optional.ofNullable(System.getenv("HIBERNATE_HBM2DDL_AUTO")).orElse("validate"));
        } else {
            String show = env("HIBERNATE_SHOW_SQL", "false");
            String hbm2ddl = env("HIBERNATE_HBM2DDL_AUTO", "");
            map.put("hibernate.show_sql", show);
            if (!hbm2ddl.isEmpty()) map.put("hibernate.hbm2ddl.auto", hbm2ddl);
        }
        return map;
    }

}
