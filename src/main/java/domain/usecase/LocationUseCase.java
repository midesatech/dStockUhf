
package domain.usecase;

import domain.gateway.LocationRepository;
import domain.model.Ubicacion;

import java.util.List;
import java.util.Objects;

public class LocationUseCase {
    private final LocationRepository repo;

    public LocationUseCase(LocationRepository repo) { this.repo = repo; }

    public Ubicacion crear(String nombre) { return crear(nombre, null); }

    public Ubicacion crear(String nombre, Long parentId) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.save(new Ubicacion(null, nombre.trim(), parentId, null));
    }

    public List<Ubicacion> listar() { return repo.findAll(); }

    public List<Ubicacion> principales() { return repo.findPrincipals(); }

    public List<Ubicacion> sububicaciones(Long parentId) {
        Objects.requireNonNull(parentId, "parentId requerido");
        return repo.findByParentId(parentId);
    }

    public void eliminar(Long id) { repo.deleteById(id); }
}
