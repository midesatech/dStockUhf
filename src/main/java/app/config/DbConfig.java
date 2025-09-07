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
        map.put("jakarta.persistence.jdbc.url", jdbcUrl());
        map.put("jakarta.persistence.jdbc.user", username());
        map.put("jakarta.persistence.jdbc.password", password());

        // optional pool sizing (Hikari used by EclipseLink/Hibernate integrations)
        String maxPool = System.getenv("HIKARI_MAX_POOL");
        if (maxPool != null && !maxPool.isEmpty()) {
            map.put("hibernate.hikari.maximumPoolSize", maxPool);
        }

        // Profile-specific safe defaults
        if ("prod".equals(profile())) {
            map.put("hibernate.show_sql", "false");
            map.put("hibernate.hbm2ddl.auto", Optional.ofNullable(System.getenv("HIBERNATE_HBM2DDL_AUTO")).orElse("validate"));
        } else {
            // dev: keep non-invasive defaults; allow env override
            String show = env("HIBERNATE_SHOW_SQL", "false");
            String hbm2ddl = env("HIBERNATE_HBM2DDL_AUTO", "");
            map.put("hibernate.show_sql", show);
            if (!hbm2ddl.isEmpty()) map.put("hibernate.hbm2ddl.auto", hbm2ddl);
        }
        return map;
    }
}
