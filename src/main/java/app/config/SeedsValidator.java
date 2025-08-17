package app.config;

import domain.model.User;
import domain.usecase.RoleUseCase;
import domain.usecase.PermissionUseCase;
import domain.usecase.UserUseCase;

import java.util.Optional;

public class SeedsValidator {
    public static void main(String[] args) {
        // Inicializar AppBootstrap en modo JPA
        AppBootstrap.init(true);

        try {
            UserUseCase userRepo = AppBootstrap.users();
            RoleUseCase roleUseCase = AppBootstrap.roleUseCase();
            PermissionUseCase permUseCase = AppBootstrap.permissionUseCase();

            // Ejecutar Seeds (ya deberían haberse creado, pero ejecutamos para test)
            Seeds.ensureInitialData(userRepo, AppBootstrap.encoder(), roleUseCase, permUseCase, true);

            // 1️⃣ Verificar permisos
            System.out.println("=== Permisos ===");
            permUseCase.findAll().forEach(p -> System.out.println(p.getId() + " : " + p.getName()));

            // 2️⃣ Verificar roles
            System.out.println("=== Roles ===");
            roleUseCase.listAll().forEach(r -> {
                System.out.println(r.getId() + " : " + r.getName());
                r.getPermissions().forEach(p -> System.out.println("   Permiso: " + p.getName()));
            });

            // 3️⃣ Verificar usuario admin
            Optional<User> admin = userRepo.findByUsername("admin");
            if (admin.isPresent()) {
                System.out.println("Usuario admin encontrado:");
                System.out.println("ID: " + admin.get().getId());
                System.out.println("Username: " + admin.get().getUsername());
                System.out.println("Roles:");
                admin.get().getRoles().forEach(r -> System.out.println("  - " + r.getName()));
            } else {
                System.out.println("Usuario admin NO encontrado");
            }

        } finally {
            // Cerrar recursos JPA
            AppBootstrap.shutdown();
        }
    }
}
