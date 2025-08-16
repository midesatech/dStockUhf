package app.config;

import domain.gateway.UserRepositoryPort;
import domain.model.Role;
import domain.model.User;
import infrastructure.adapter.database.jpa.JpaRoleRepositoryAdapter;
import infrastructure.adapter.database.jpa.JpaPermissionRepositoryAdapter;
import domain.gateway.PasswordEncoderPort;

import java.util.HashSet;
import java.util.Set;

public class Seeds {

    public static void ensureInitialData(UserRepositoryPort users, PasswordEncoderPort encoder,
                                         JpaRoleRepositoryAdapter roleRepo, JpaPermissionRepositoryAdapter permRepo, boolean jpaMode){
        try{
            if(!jpaMode) return;
            // create permissions
            if(permRepo.count()==0){
                permRepo.save("CATALOG_READ");
                permRepo.save("CATALOG_WRITE");
                permRepo.save("INVENTORY_ASSIGN");
                permRepo.save("USER_MANAGE");
                permRepo.save("ROLE_MANAGE");
            }
            // create roles
            if(roleRepo.count()==0){
                Long adminRoleId = roleRepo.save("ADMINISTRADOR");
                Long userRoleId = roleRepo.save("USUARIO");
                Long advRoleId = roleRepo.save("USUARIO_AVANZADO");
                // assign all perms to admin
                for(String p : permRepo.findAllNames()){
                    roleRepo.addPermission(adminRoleId, p);
                }
            }
            // create admin user if not exists
            if(!users.existsByUsername("ADMIN")){
                User u = new User();
                u.setUsername("ADMIN");
                u.setPasswordHash(encoder.encode("admin123"));
                u.setSystemUser(true);
                Set<Role> roles = new HashSet<>();
                roles.add(new Role(null, "ADMINISTRADOR"));
                u.setRoles(roles);
                users.save(u);
            }
        }catch(Exception ex){
            System.out.println("Seed error: " + ex.getMessage());
        }
    }
}