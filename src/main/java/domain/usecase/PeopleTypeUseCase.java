package domain.usecase;

import domain.gateway.PeopleTypeRepository;
import domain.model.PeopleType;

import java.util.List;
import java.util.Optional;

public class PeopleTypeUseCase {
    private final PeopleTypeRepository repo;
    public PeopleTypeUseCase(PeopleTypeRepository repo) { this.repo = repo; }

    public PeopleType crear(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("Nombre requerido");
        return repo.save(new PeopleType(null, nombre.trim()));
    }
    public PeopleType actualizar(Long id, String nombre) {
        if (id == null) throw new IllegalArgumentException("ID requerido");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        return repo.update(new PeopleType(id, nombre.trim()));
    }
    public void eliminar(Long id) { repo.deleteById(id); }
    public List<PeopleType> listar() { return repo.findAll(); }
    public Optional<PeopleType> findById(Long id) { return repo.findById(id); }
}
