package app.config;

import domain.gateway.PasswordEncoderPort;
import domain.gateway.UserRepositoryPort;
import infrastructure.adapter.database.jpa.CategoriaRepositoryAdapter;
import infrastructure.adapter.database.jpa.JpaRoleRepositoryAdapter;
import infrastructure.adapter.database.jpa.JpaPermissionRepositoryAdapter;
import infrastructure.adapter.database.jpa.JpaUserRepositoryAdapter;
import infrastructure.adapter.database.memory.InMemoryUserRepositoryAdapter;
import infrastructure.adapter.security.BCryptPasswordEncoderAdapter;
import infrastructure.adapter.security.SimpleAuthServiceAdapter;
import infrastructure.persistence.JPAUtil;
import domain.gateway.AuthServicePort;

public class AppBootstrap {
    private static boolean jpaMode = false;
    private static UserRepositoryPort userRepository;
    private static PasswordEncoderPort encoder;
    private static AuthServicePort auth;
    private static JpaRoleRepositoryAdapter roleRepo;
    private static JpaPermissionRepositoryAdapter permRepo;

    public static void init(boolean useJpa) {
        jpaMode = useJpa;
        encoder = new BCryptPasswordEncoderAdapter();

        if (useJpa) {
            // ensure DB exists (uses defaults from persistence.xml)
            infrastructure.persistence.DatabaseCreator.ensureDatabaseExists("jdbc:mariadb://localhost:3306/inventario", "root", "");

            JPAUtil.init();
            userRepository = new JpaUserRepositoryAdapter(JPAUtil.getEmf(), encoder);
            roleRepo = new JpaRoleRepositoryAdapter(JPAUtil.getEmf());
            categoriaUseCase = new domain.usecase.CategoriaUseCase(new CategoriaRepositoryAdapter(JPAUtil.getEmf()));
// wire ubicacion/empleado/producto usecases
            domain.usecase.UbicacionUseCase ubicUse = new domain.usecase.UbicacionUseCase(new infrastructure.adapter.database.jpa.UbicacionRepositoryAdapter(JPAUtil.getEmf()));
            domain.usecase.EmpleadoUseCase empUse = new domain.usecase.EmpleadoUseCase(new infrastructure.adapter.database.jpa.EmpleadoRepositoryAdapter(JPAUtil.getEmf()));
            domain.usecase.ProductoUseCase prodUse = new domain.usecase.ProductoUseCase(new infrastructure.adapter.database.jpa.ProductoRepositoryAdapter(JPAUtil.getEmf()));
// expose via static accessors
            ubicacionUseCase = ubicUse;
            empleadoUseCase = empUse;
            productoUseCase = prodUse;

            // other repositories
            // Ubicacion
            // note: classes used from infrastructure.adapter.database.jpa

            permRepo = new JpaPermissionRepositoryAdapter(JPAUtil.getEmf());
        } else {
            userRepository = new InMemoryUserRepositoryAdapter();
        }
        auth = new SimpleAuthServiceAdapter(userRepository, encoder);

        // seeds (create roles/perms/users if not present)
        Seeds.ensureInitialData(userRepository, encoder, roleRepo, permRepo, jpaMode);
    }

    public static void shutdown() {
        if (jpaMode) JPAUtil.shutdown();
    }

    public static UserRepositoryPort users() {
        return userRepository;
    }

    public static PasswordEncoderPort encoder() {
        return encoder;
    }

    public static AuthServicePort auth() {
        return auth;
    }

    public static JpaRoleRepositoryAdapter roleRepo() {
        return roleRepo;
    }

    public static JpaPermissionRepositoryAdapter permRepo() {
        return permRepo;
    }

    public static boolean isJpaMode() {
        return jpaMode;
    }

    // Categoria use case wiring
    private static domain.usecase.CategoriaUseCase categoriaUseCase;

    public static domain.usecase.CategoriaUseCase categoriaUseCase() {
        return categoriaUseCase;
    }

    private static domain.usecase.UbicacionUseCase ubicacionUseCase;
    private static domain.usecase.EmpleadoUseCase empleadoUseCase;
    private static domain.usecase.ProductoUseCase productoUseCase;

    public static domain.usecase.UbicacionUseCase ubicacionUseCase() {
        return ubicacionUseCase;
    }

    public static domain.usecase.EmpleadoUseCase empleadoUseCase() {
        return empleadoUseCase;
    }

    public static domain.usecase.ProductoUseCase productoUseCase() {
        return productoUseCase;
    }

}