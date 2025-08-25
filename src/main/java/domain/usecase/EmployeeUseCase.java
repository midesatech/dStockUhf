
package domain.usecase;

import domain.gateway.EmployeeRepository;
import domain.model.Employee;
import domain.model.TipoDocumento;

import java.util.List;
import java.util.Optional;

public class EmployeeUseCase {
    private final EmployeeRepository repo;

    public EmployeeUseCase(EmployeeRepository repo) {
        this.repo = repo;
    }

    public Employee save(Employee e) {
        if (e.getFullName() == null || e.getFullName().isBlank())
            throw new IllegalArgumentException("Nombre requerido");

        if (e.getDocType() == null)
            throw new IllegalArgumentException("Tipo de documento requerido");

        if (e.getDocNumber() == null || e.getDocNumber().isBlank())
            throw new IllegalArgumentException("Número de documento requerido");

        if (e.getBirthDate() == null)
            throw new IllegalArgumentException("Fecha de nacimiento requerida");

        if (e.getBloodType() == null)
            throw new IllegalArgumentException("Tipo de sangre requerido");

        // Normalizaciones (opcionales)
        e.setFullName(e.getFullName().trim());
        e.setLastName(e.getLastName().trim());
        if (e.getEmail() != null && e.getEmail().isBlank()) e.setEmail(null);
        if (e.getPhone() != null && e.getPhone().isBlank()) e.setPhone(null);
        if (e.getEpc() != null && e.getEpc().isBlank()) e.setEpc(null);

        return repo.save(e);
    }

    public List<Employee> listar() { return repo.findAll(); }

    public void eliminar(Long id) { repo.deleteById(id); }

    public List<Employee> buscar(TipoDocumento tipoDocumento, String numeroDocumento, String nombre, String apellido, String codigo) {
        return repo.search(tipoDocumento, numeroDocumento, nombre, apellido, codigo);
    }

    public Employee asignarEpc(Long empleadoId, String epc) {
        Employee e = repo.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        if (e.getEpc() != null && !e.getEpc().isBlank()) {
            throw new IllegalStateException("Empleado ya tiene un EPC asignado");
        }

        e.setEpc(epc);
        return repo.save(e);
    }

    public Optional<Employee> findByEpc(String epc) {
        return repo.findByEpc(epc);
    }

}
