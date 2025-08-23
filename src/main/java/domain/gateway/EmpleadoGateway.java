
package domain.gateway;

import domain.model.Empleado;
import domain.model.TipoDocumento;

import java.util.List;
import java.util.Optional;

public interface EmpleadoGateway {
    Empleado save(Empleado e);

    List<Empleado> findAll();

    void deleteById(Long id);

    Optional<Empleado> findById(Long id);

    List<Empleado> search(TipoDocumento tipoDocumento, String numeroDocumento, String nombre, String apellido, String codigo);

    Optional<Empleado> findByEpc(String epc);
}
