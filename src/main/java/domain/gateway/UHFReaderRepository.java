package domain.gateway;

import domain.model.UHFReader;
import domain.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public interface UHFReaderRepository {
    UHFReader save(UHFReader l);
    List<UHFReader> findAll();
    void deleteById(Long id);
    Optional<UHFReader> findById(Long id);
    UHFReader update(UHFReader l);
    Optional<UHFReader> findByCodigo(String codigo);
    List<UHFReader> findByFilters(String codigo, Long ubicacionId);
    List<UHFReader> buscar(String codigo, Ubicacion ubicacion);
}
