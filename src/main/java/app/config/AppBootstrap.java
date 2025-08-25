package app.config;

import domain.gateway.*;
import domain.model.tag.ESerialMode;
import domain.usecase.*;
import domain.usecase.tag.*;
import help.SerialConstants;
import infrastructure.adapter.database.jpa.*;
import infrastructure.adapter.database.memory.InMemoryUserRepositoryAdapter;
import infrastructure.adapter.security.BCryptPasswordEncoderAdapter;
import infrastructure.adapter.security.SimpleAuthServiceAdapter;
import infrastructure.adapter.serial.SerialFactory;
import infrastructure.adapter.serial.SerialPortRepositoryAdapter;
import infrastructure.adapter.serial.TagOperationsAdapter;
import infrastructure.persistence.JPAUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

public class AppBootstrap {
    private static final Logger infLog = LogManager.getLogger("infLogger");
    private static final Logger logger = LogManager.getLogger(AppBootstrap.class);
    private static final Logger errLog = LogManager.getLogger("errLogger");

    private static boolean jpaMode = false;

    // Core services
    private static UserRepository userRepository;
    private static PasswordEncoderPort encoder;
    private static AuthServiceRepository auth;
    private static SerialFactory serialFactory;
    private static SerialPortRepository serialPortRepository;
    private static TagOperationsPort tagOperationsPort;

    //Repositories adapters
    private static RoleRepositoryAdapter roleRepo;
    private static PermissionRepositoryAdapter permRepo;
    private static UHFReaderRepositoryAdapter lectorUHFRepo;
    private static UHFTagRepositoryAdapter UHFTagRepositoryAdapter;

    // UseCases
    private static CategoriaUseCase categoriaUseCase;
    private static LocationUseCase locationUseCase;
    private static EmployeeUseCase employeeUseCase;
    private static EquipmentUseCase equipmentUseCase;
    private static RoleUseCase roleUseCase;
    private static PermissionUseCase permissionUseCase;
    private static UserUseCase userUseCase;
    private static UHFReaderUseCase UHFReaderUseCase;
    private static TagOperationsUseCase tagOperationsUseCase;
    private static SerialUseCase serialUseCase;
    private static SerialCommunicationUseCase serialCommunicationUseCase;
    private static ReaderUseCase readerUseCase;
    private static OperationsUseCase operationsUseCase;
    private static TagUHFUseCase tagUHFUseCase;
    private static ReadTagUseCase readTagUseCase;

    public static void init(boolean useJpa) {
        jpaMode = useJpa;
        encoder = new BCryptPasswordEncoderAdapter();
        serialFactory = new SerialFactory();

        Optional<AppConfig> appConfig = loadProperties();

        if (useJpa) {
            // ensure DB exists (uses defaults from persistence.xml)
            infrastructure.persistence.DatabaseCreator.ensureDatabaseExists(
                    "jdbc:mariadb://localhost:3306/inventario", "root", "");

            JPAUtil.init();
            userRepository = new UserRepositoryAdapter(JPAUtil.getEmf(), encoder);
            roleRepo = new RoleRepositoryAdapter(JPAUtil.getEmf());
            permRepo = new PermissionRepositoryAdapter(JPAUtil.getEmf());
            lectorUHFRepo = new UHFReaderRepositoryAdapter(JPAUtil.getEmf());


            categoriaUseCase = new CategoriaUseCase(new CategoryRepositoryAdapter(JPAUtil.getEmf()));
            locationUseCase = new LocationUseCase(new LocationRepositoryAdapter(JPAUtil.getEmf()));
            employeeUseCase = new EmployeeUseCase(new EmployeeRepositoryAdapter(JPAUtil.getEmf()));
            equipmentUseCase = new EquipmentUseCase(new EquipmentRepositoryAdapter(JPAUtil.getEmf()));

            roleUseCase = new RoleUseCase(roleRepo);
            permissionUseCase = new PermissionUseCase(permRepo);
            UHFReaderUseCase = new UHFReaderUseCase(lectorUHFRepo);
            UHFTagRepositoryAdapter = new UHFTagRepositoryAdapter(JPAUtil.getEmf());
        } else {
            userRepository = new InMemoryUserRepositoryAdapter();
        }

        userUseCase = new UserUseCase(userRepository, encoder);

        auth = new SimpleAuthServiceAdapter(userRepository, encoder);

        // seeds (create roles/perms/users if not present)
        Seeds.ensureInitialData(userUseCase, encoder,
                // eliminamos roleRepo y permRepo directos, ahora usamos los UseCases
                roleUseCase, permissionUseCase, jpaMode);

        serialPortRepository = new SerialPortRepositoryAdapter(serialFactory);
        tagOperationsPort = new TagOperationsAdapter(serialPortRepository);
        serialCommunicationUseCase = new SerialCommunicationUseCase(serialPortRepository);
        tagOperationsUseCase = new TagOperationsUseCase(tagOperationsPort);
        operationsUseCase = new OperationsUseCase(tagOperationsUseCase, serialCommunicationUseCase);
        readerUseCase = new ReaderUseCase(operationsUseCase);
        tagUHFUseCase = new TagUHFUseCase(UHFTagRepositoryAdapter);
        readTagUseCase = new ReadTagUseCase(operationsUseCase);
        appConfig.ifPresent(config -> {
            readerUseCase.setAppConfig(config);
            readTagUseCase.setAppConfig(config);
        });
    }

    private static Optional<AppConfig> loadProperties() {
        Properties prop = new Properties();

        String externalPath = "application.properties"; // Puedes poner ruta absoluta si quieres

        try (InputStream externalInput = new FileInputStream(externalPath)) {
            prop.load(externalInput);
            infLog.info("Loaded external application.properties from: " + externalPath);
        } catch (IOException ex) {
            errLog.warn("External application.properties not found or failed to load. Trying internal resource.");

            // Intentar cargar el interno
            try (InputStream internalInput = AppBootstrap.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (internalInput == null) {
                    errLog.fatal("Sorry, unable to find application.properties (neither external nor internal).");
                    return Optional.empty();
                }
                prop.load(internalInput);
                infLog.info("Loaded internal application.properties from JAR.");
            } catch (IOException e) {
                errLog.fatal("Error loading internal application.properties: " + e.getMessage());
                e.printStackTrace();
                return Optional.empty();
            }
        }

        try {

            String comPort = prop.getProperty("app.comPort");
            int timeOutEnrolar = Optional.ofNullable(prop.getProperty("app.timeoutEnrolar"))
                    .map(Integer::parseInt)
                    .orElse(10000);

            int db = Optional.ofNullable(prop.getProperty("app.db"))
                    .map(Integer::parseInt)
                    .orElse(SerialConstants.DEFAULT_BAUD_RATE);

            int mode = Optional.ofNullable(prop.getProperty("app.mode"))
                    .map(Integer::parseInt)
                    .orElse(0);

            String stage = Optional.ofNullable(prop.getProperty("app.stage"))
                    .orElse("99");

            String device = Optional.ofNullable(prop.getProperty("app.device"))
                    .orElse("999");

            SerialConfig serialConfig = new SerialConfig(comPort, timeOutEnrolar, db, ESerialMode.fromValue(mode));

            return Optional.of(new AppConfig(serialConfig, stage, device));

        } catch (Exception e) {
            errLog.fatal("Error parsing properties: " + e.getMessage());
            e.printStackTrace();
        }

        return Optional.empty();
    }

    public static void shutdown() {
        if (jpaMode) JPAUtil.shutdown();
    }

    // === Accessors ===
    public static UserUseCase users() {
        return userUseCase;
    }

    public static PasswordEncoderPort encoder() {
        return encoder;
    }

    public static AuthServiceRepository auth() {
        return auth;
    }

    public static CategoriaUseCase categoriaUseCase() {
        return categoriaUseCase;
    }

    public static LocationUseCase ubicacionUseCase() {
        return locationUseCase;
    }

    public static EmployeeUseCase empleadoUseCase() {
        return employeeUseCase;
    }

    public static EquipmentUseCase equipmentUseCase() {
        return equipmentUseCase;
    }

    public static RoleUseCase roleUseCase() {
        return roleUseCase;
    }

    public static PermissionUseCase permissionUseCase() {
        return permissionUseCase;
    }

    public static boolean isJpaMode() {
        return jpaMode;
    }

    public static UHFReaderUseCase lectorUHFUseCase() {
        return UHFReaderUseCase;
    }

    public static ReaderUseCase readerUseCase() {
        return readerUseCase;
    }

    public static TagUHFUseCase tagUhfUsecase() {
        return tagUHFUseCase;
    }

    public static ReadTagUseCase readTagUseCase() {
        return readTagUseCase;
    }

}
