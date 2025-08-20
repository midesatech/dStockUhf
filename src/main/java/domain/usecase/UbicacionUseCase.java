
package domain.usecase;

import domain.gateway.UbicacionGateway;
import domain.model.Ubicacion;

import java.util.List;

public class UbicacionUseCase {
    private final UbicacionGateway repo;

    public UbicacionUseCase(UbicacionGateway repo) {
        this.repo = repo;
    }

    public Ubicacion crear(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.save(new Ubicacion(null, nombre.trim()));
    }

    public List<Ubicacion> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
