
package domain.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Role {
    private Long id;
    private String name;
    private Set<Permission> permissions = new HashSet<>();

    public Role() {
    }

    public Role(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Role(String name) {
        this.name = name;
    }

    // 🔹 Nuevo constructor con permisos
    public Role(Long id, String name, Set<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    // --- getters ---
    public Long getId() {
        return id;
    }

    // opcional: deja privado o protégelo para evitar mutaciones externas
    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    // --- utilidades de permisos ---
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions != null ? permissions : new HashSet<>();
    }

    // --- equals & hashCode basados en "name" (porque es único en DB) ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role)) return false;
        Role role = (Role) o;
        return Objects.equals(name, role.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    // --- para debug ---
    @Override
    public String toString() {
        return "Role{id=" + id + ", name='" + name + "', permissions=" + permissions + '}';
    }
}