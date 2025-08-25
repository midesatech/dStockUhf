package domain.usecase;

import domain.gateway.UHFReaderRepository;
import domain.model.UHFReader;
import domain.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public class UHFReaderUseCase {
    private final UHFReaderRepository repo;

    public UHFReaderUseCase(UHFReaderRepository repo) {
        this.repo = repo;
    }

    public UHFReader crear(String codigo, String descripcion, Ubicacion ubicacion) {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código requerido");
        if (ubicacion == null)
            throw new IllegalArgumentException("Ubicación requerida");

        return repo.save(new UHFReader(
                null,
                codigo.trim(),
                descripcion != null ? descripcion.trim() : null,
                ubicacion
        ));
    }

    public UHFReader actualizar(Long id, String codigo, String descripcion, Ubicacion ubicacion) {
        if (id == null)
            throw new IllegalArgumentException("ID requerido para actualizar");
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código requerido");
        if (ubicacion == null)
            throw new IllegalArgumentException("Ubicación requerida");

        return repo.update(new UHFReader(
                id,
                codigo.trim(),
                descripcion != null ? descripcion.trim() : null,
                ubicacion
        ));
    }

    public List<UHFReader> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public List<UHFReader> buscar(String codigo, Ubicacion ubicacion) {
        return repo.buscar(codigo, ubicacion);
    }

    public Optional<UHFReader> findById(Long id) {
        return repo.findById(id);
    }

    public Optional<UHFReader> findByCodigo(String codigo) {
        return repo.findByCodigo(codigo);
    }
}
