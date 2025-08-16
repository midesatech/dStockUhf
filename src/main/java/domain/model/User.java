
package domain.model;

import java.util.HashSet;
import java.util.Set;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private boolean systemUser;
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.systemUser = false;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public User(Long id, String username, String passwordHash, boolean systemUser) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.systemUser = systemUser;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isSystemUser() {
        return systemUser;
    }

    public void setSystemUser(boolean systemUser) {
        this.systemUser = systemUser;
    }

    public java.util.Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(java.util.Set<Role> roles) {
        this.roles = roles;
    }
}
