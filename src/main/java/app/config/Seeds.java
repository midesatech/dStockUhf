package app.config;

import domain.gateway.UserRepositoryPort;
import domain.gateway.PasswordEncoderPort;
import domain.usecase.RoleUseCase;
import domain.usecase.PermissionUseCase;
import domain.model.User;
import domain.model.Role;
import domain.model.Permission;

public class Seeds {

    public static void ensureInitialData(
            UserRepositoryPort userRepository,
            PasswordEncoderPort encoder,
            RoleUseCase roleUseCase,
            PermissionUseCase permissionUseCase,
            boolean jpaMode) {

        if (!jpaMode) {
            // ⚠️ si estamos en modo memoria puedes decidir no sembrar datos
            System.out.println("Skipping seeds (in-memory mode)");
            return;
        }

        // === Crear permisos iniciales ===
        if (permissionUseCase.count() == 0) {
            permissionUseCase.create(new Permission("READ_CATEGORIA"));
            permissionUseCase.create(new Permission("WRITE_CATEGORIA"));
            permissionUseCase.create(new Permission("READ_PRODUCTO"));
            permissionUseCase.create(new Permission("WRITE_PRODUCTO"));
            System.out.println("🔑 Permisos iniciales creados");
        }

        // === Crear roles iniciales ===
        if (roleUseCase.count() == 0) {
            Role admin = new Role("ADMIN");
            Role user = new Role("USER");

            // asignar permisos al rol admin (unwrap Optional con orElseThrow)
            Permission readCat = permissionUseCase.findByName("READ_CATEGORIA");
            Permission writeCat = permissionUseCase.findByName("WRITE_CATEGORIA");
            Permission readProd = permissionUseCase.findByName("READ_PRODUCTO");
            Permission writeProd = permissionUseCase.findByName("WRITE_PRODUCTO");

            admin.addPermission(readCat);
            admin.addPermission(writeCat);
            admin.addPermission(readProd);
            admin.addPermission(writeProd);

            roleUseCase.create(admin);
            roleUseCase.create(user);

            System.out.println("👤 Roles iniciales creados");
        }

        // === Crear usuario admin ===
        if (userRepository.count() == 0) {
            User adminUser = new User("admin", encoder.encode("admin123"));

            Role adminRole = roleUseCase.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            adminUser.addRole(adminRole);
            userRepository.save(adminUser);

            System.out.println("👤 Usuario admin creado");
        }
    }
}