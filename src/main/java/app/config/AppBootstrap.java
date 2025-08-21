package app.config;

import domain.gateway.*;
import domain.usecase.*;
import domain.usecase.tag.*;
import infrastructure.adapter.database.jpa.*;
import infrastructure.adapter.database.memory.InMemoryUserRepositoryAdapter;
import infrastructure.adapter.security.BCryptPasswordEncoderAdapter;
import infrastructure.adapter.security.SimpleAuthServiceAdapter;
import infrastructure.adapter.serial.SerialFactory;
import infrastructure.adapter.serial.SerialPortAdapter;
import infrastructure.adapter.serial.TagOperationsAdapter;
import infrastructure.persistence.JPAUtil;

public class AppBootstrap {
    private static boolean jpaMode = false;

    // Core services
    private static UserRepository userRepository;
    private static PasswordEncoderPort encoder;
    private static AuthServicePort auth;
    private static SerialFactory serialFactory;
    private static SerialPort serialPort;
    private static TagOperationsPort tagOperationsPort;

    //Repositories adapters
    private static JpaRoleRepositoryAdapter roleRepo;
    private static JpaPermissionRepositoryAdapter permRepo;
    private static LectorUHFRepositoryAdapter lectorUHFRepo;

    // UseCases
    private static CategoriaUseCase categoriaUseCase;
    private static UbicacionUseCase ubicacionUseCase;
    private static EmpleadoUseCase empleadoUseCase;
    private static EquipmentUseCase equipmentUseCase;
    private static RoleUseCase roleUseCase;
    private static PermissionUseCase permissionUseCase;
    private static UserUseCase userUseCase;
    private static LectorUHFUseCase lectorUHFUseCase;
    private static TagOperationsUseCase tagOperationsUseCase;
    private static SerialUseCase serialUseCase;
    private static SerialCommunicationUseCase serialCommunicationUseCase;
    private static ReaderUseCase readerUseCase;
    private static OperationsUseCase operationsUseCase;

    public static void init(boolean useJpa) {
        jpaMode = useJpa;
        encoder = new BCryptPasswordEncoderAdapter();
        serialFactory = new SerialFactory();

        if (useJpa) {
            // ensure DB exists (uses defaults from persistence.xml)
            infrastructure.persistence.DatabaseCreator.ensureDatabaseExists(
                    "jdbc:mariadb://localhost:3306/inventario", "root", "");

            JPAUtil.init();
            userRepository = new JpaUserRepositoryAdapter(JPAUtil.getEmf(), encoder);
            roleRepo = new JpaRoleRepositoryAdapter(JPAUtil.getEmf());
            permRepo = new JpaPermissionRepositoryAdapter(JPAUtil.getEmf());
            lectorUHFRepo = new LectorUHFRepositoryAdapter(JPAUtil.getEmf());


            categoriaUseCase = new CategoriaUseCase(new CategoriaRepositoryAdapter(JPAUtil.getEmf()));
            ubicacionUseCase = new UbicacionUseCase(new UbicacionRepositoryAdapter(JPAUtil.getEmf()));
            empleadoUseCase = new EmpleadoUseCase(new EmpleadoRepositoryAdapter(JPAUtil.getEmf()));
            equipmentUseCase = new EquipmentUseCase(new EquipmentRepositoryAdapter(JPAUtil.getEmf()));

            roleUseCase = new RoleUseCase(roleRepo);
            permissionUseCase = new PermissionUseCase(permRepo);
            lectorUHFUseCase = new LectorUHFUseCase(lectorUHFRepo);

        } else {
            userRepository = new InMemoryUserRepositoryAdapter();
        }

        userUseCase = new UserUseCase(userRepository, encoder);

        auth = new SimpleAuthServiceAdapter(userRepository, encoder);

        // seeds (create roles/perms/users if not present)
        Seeds.ensureInitialData(userUseCase, encoder,
                // eliminamos roleRepo y permRepo directos, ahora usamos los UseCases
                roleUseCase, permissionUseCase, jpaMode);

        serialPort = new SerialPortAdapter(serialFactory);
        tagOperationsPort = new TagOperationsAdapter(serialPort);
        serialCommunicationUseCase = new SerialCommunicationUseCase(serialPort);
        tagOperationsUseCase = new TagOperationsUseCase(tagOperationsPort);
        operationsUseCase = new OperationsUseCase(tagOperationsUseCase, serialCommunicationUseCase);
        readerUseCase = new ReaderUseCase(operationsUseCase);
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

    public static AuthServicePort auth() {
        return auth;
    }

    public static CategoriaUseCase categoriaUseCase() {
        return categoriaUseCase;
    }

    public static UbicacionUseCase ubicacionUseCase() {
        return ubicacionUseCase;
    }

    public static EmpleadoUseCase empleadoUseCase() {
        return empleadoUseCase;
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

    public static LectorUHFUseCase lectorUHFUseCase() {
        return lectorUHFUseCase;
    }

    public static ReaderUseCase readerUseCase() {
        return readerUseCase;
    }
}
