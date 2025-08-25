
package domain.gateway;

import domain.model.Employee;
import domain.model.TipoDocumento;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Employee save(Employee e);

    List<Employee> findAll();

    void deleteById(Long id);

    Optional<Employee> findById(Long id);

    List<Employee> search(TipoDocumento tipoDocumento, String numeroDocumento, String nombre, String apellido, String codigo);

    Optional<Employee> findByEpc(String epc);
}
