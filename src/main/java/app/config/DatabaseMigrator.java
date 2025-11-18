package app.config;

import app.config.DbConfig;
import org.flywaydb.core.Flyway;

public final class DatabaseMigrator {

    private DatabaseMigrator() {}

    public static void migrate() {
        String url = DbConfig.jdbcUrl();
        String user = DbConfig.username();
        String pass = DbConfig.password();

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true) // si ya tienes una BD con tablas existentes
                .load();

        flyway.migrate();
    }
}
