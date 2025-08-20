package domain.gateway;

import domain.model.LectorUHF;
import domain.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public interface LectorUHFRepository {
    LectorUHF save(LectorUHF l);
    List<LectorUHF> findAll();
    void deleteById(Long id);
    Optional<LectorUHF> findById(Long id);
    LectorUHF update(LectorUHF l);
    Optional<LectorUHF> findByCodigo(String codigo);
    List<LectorUHF> findByFilters(String codigo, Long ubicacionId);
    List<LectorUHF> buscar(String codigo, Ubicacion ubicacion);
}
