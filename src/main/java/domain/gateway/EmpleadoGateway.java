
package domain.gateway;
import domain.model.Empleado;
import java.util.List;
import java.util.Optional;
public interface EmpleadoGateway { Empleado save(Empleado e); List<Empleado> findAll(); void deleteById(Long id); Optional<Empleado> findById(Long id); }
