
package domain.gateway;

import domain.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public interface UbicacionGateway {
    Ubicacion save(Ubicacion u);

    List<Ubicacion> findAll();

    void deleteById(Long id);

    Optional<Ubicacion> findById(Long id);
}
