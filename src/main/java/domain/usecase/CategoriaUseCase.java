
package domain.usecase;

import domain.gateway.CategoryRepository;
import domain.model.Category;

import java.util.List;
import java.util.Optional;

public class CategoriaUseCase {
    private final CategoryRepository repo;

    public CategoriaUseCase(CategoryRepository repo) {
        this.repo = repo;
    }

    public Category crear(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.save(new Category(null, nombre.trim()));
    }

    public List<Category> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public Optional<Category> findById(Long id) {
        return repo.findById(id);
    }

    public Category actualizar(Long id, String nombre) {
        if (id == null) throw new IllegalArgumentException("ID requerido para actualizar");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.update(new Category(id, nombre.trim()));
    }
}
