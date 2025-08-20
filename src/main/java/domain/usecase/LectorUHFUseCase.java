package domain.usecase;

import domain.gateway.LectorUHFRepository;
import domain.model.LectorUHF;
import domain.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public class LectorUHFUseCase {
    private final LectorUHFRepository repo;

    public LectorUHFUseCase(LectorUHFRepository repo) {
        this.repo = repo;
    }

    public LectorUHF crear(String codigo, String descripcion, Ubicacion ubicacion) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código requerido");
        if (ubicacion == null)
            throw new IllegalArgumentException("Ubicación requerida");

        return repo.save(new LectorUHF(
                null,
                codigo.trim(),
                descripcion != null ? descripcion.trim() : null,
                ubicacion
        ));
    }

    public LectorUHF actualizar(Long id, String codigo, String descripcion, Ubicacion ubicacion) {
        if (id == null)
            throw new IllegalArgumentException("ID requerido para actualizar");
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código requerido");
        if (ubicacion == null)
            throw new IllegalArgumentException("Ubicación requerida");

        return repo.update(new LectorUHF(
                id,
                codigo.trim(),
                descripcion != null ? descripcion.trim() : null,
                ubicacion
        ));
    }

    public List<LectorUHF> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public List<LectorUHF> buscar(String codigo, Ubicacion ubicacion) {
        return repo.buscar(codigo, ubicacion);
    }

    public Optional<LectorUHF> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<LectorUHF> findByCodigo(String codigo) {
        return repo.findByCodigo(codigo);
    }
}
