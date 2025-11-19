package app.config;

import org.flywaydb.core.Flyway;

public final class DatabaseMigrator {

    private DatabaseMigrator() {}

    public static void migrate() {
        String url = PropertyConfigService.getUrl();
        String user = PropertyConfigService.getUser();
        String pass = PropertyConfigService.getPassword();

        Flyway flyway = Flyway.configure()
                .dataSource(url, user, pass)
                .locations("classpath:db/migrations")
                .baselineOnMigrate(true) // si ya tienes una BD con tablas existentes
                .load();

        flyway.migrate();
    }
}
