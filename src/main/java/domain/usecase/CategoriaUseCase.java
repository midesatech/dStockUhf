
package domain.usecase;

import domain.gateway.CategoriaGateway;
import domain.model.Categoria;

import java.util.List;
import java.util.Optional;

public class CategoriaUseCase {
    private final CategoriaGateway repo;

    public CategoriaUseCase(CategoriaGateway repo) {
        this.repo = repo;
    }

    public Categoria crear(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.save(new Categoria(null, nombre.trim()));
    }

    public List<Categoria> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public Optional<Categoria> findById(Long id) {
        return repo.findById(id);
    }

    public Categoria actualizar(Long id, String nombre) {
        if (id == null) throw new IllegalArgumentException("ID requerido para actualizar");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.update(new Categoria(id, nombre.trim()));
    }
}
