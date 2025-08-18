
package domain.usecase;

import domain.gateway.EmpleadoGateway;
import domain.model.Empleado;

import java.util.List;

public class EmpleadoUseCase {
    private final EmpleadoGateway repo;

    public EmpleadoUseCase(EmpleadoGateway repo) {
        this.repo = repo;
    }

    public Empleado save(Empleado e) {
        if (e.getFullName() == null || e.getFullName().isBlank())
            throw new IllegalArgumentException("Nombre requerido");

        if (e.getTipoDocumento() == null)
            throw new IllegalArgumentException("Tipo de documento requerido");

        if (e.getNumeroDocumento() == null || e.getNumeroDocumento().isBlank())
            throw new IllegalArgumentException("Número de documento requerido");

        if (e.getFechaNacimiento() == null)
            throw new IllegalArgumentException("Fecha de nacimiento requerida");

        if (e.getTipoSanguineo() == null)
            throw new IllegalArgumentException("Tipo de sangre requerido");

        // Normalizaciones (opcionales)
        e.setFullName(e.getFullName().trim());
        e.setLastName(e.getLastName().trim());
        if (e.getEmail() != null && e.getEmail().isBlank()) e.setEmail(null);
        if (e.getTelefono() != null && e.getTelefono().isBlank()) e.setTelefono(null);
        if (e.getCodigo() != null && e.getCodigo().isBlank()) e.setCodigo(null);

        return repo.save(e);
    }

    public List<Empleado> listar() { return repo.findAll(); }

    public void eliminar(Long id) { repo.deleteById(id); }
}
