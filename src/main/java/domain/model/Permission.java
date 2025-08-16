
package domain.model;

public class Permission {
    private Long id;
    private String name;

    public Permission() {
    }

    public Permission(String name) {
        this.name = name;
    }

    public Permission(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name; // o getName()
    }
}
