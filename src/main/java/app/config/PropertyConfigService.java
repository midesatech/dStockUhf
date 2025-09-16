package app.config;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

public final class PropertyConfigService {
    // ~/.dstockuhf/app.properties  (portable + not in repo)
    private static final String DIR_NAME = ".dstockuhf";
    private static final String FILE_NAME = "app.properties";

    // Keys we persist
    public static final String KEY_DB_HOST = "db.host";
    public static final String KEY_DB_PORT = "db.port";
    public static final String KEY_DB_USER = "db.user";
    public static final String KEY_DB_PASSWORD = "db.password";

    private static Properties cached;

    private PropertyConfigService() {}

    private static Path configDir() {
        return Path.of(System.getProperty("user.home"), DIR_NAME);
    }

    private static Path configFile() {
        return configDir().resolve(FILE_NAME);
    }

    public static boolean exists() {
        return Files.exists(configFile());
    }

    public static synchronized Properties load() {
        if (cached != null) return cached;
        Properties p = new Properties();
        if (exists()) {
            try (InputStream in = Files.newInputStream(configFile())) {
                p.load(in);
            } catch (IOException e) {
                throw new RuntimeException("Error reading "+configFile()+": "+e.getMessage(), e);
            }
        }
        cached = p;
        return cached;
    }

    public static synchronized void save(String host, String port, String user, String password) {
        Objects.requireNonNull(host, "host");
        Objects.requireNonNull(port, "port");
        Objects.requireNonNull(user, "user");
        if (!Files.exists(configDir())) {
            try { Files.createDirectories(configDir()); }
            catch (IOException e) { throw new RuntimeException("Cannot create config dir: "+configDir(), e); }
        }
        Properties p = new Properties();
        p.setProperty(KEY_DB_HOST, host.trim());
        p.setProperty(KEY_DB_PORT, port.trim());
        p.setProperty(KEY_DB_USER, user.trim());
        // store **as-is**, PasswordField masks UI only; file is not encrypted
        p.setProperty(KEY_DB_PASSWORD, password == null ? "" : password);

        try (OutputStream out = Files.newOutputStream(configFile())) {
            p.store(out, "dStockUhf App Settings");
        } catch (IOException e) {
            throw new RuntimeException("Error writing "+configFile()+": "+e.getMessage(), e);
        }
        cached = p;
    }

    public static String get(String key, String defVal) {
        return load().getProperty(key, defVal);
    }
}
