
package domain.usecase;

import domain.gateway.PeopleRepository;
import domain.model.People;
import domain.model.PeopleType;
import domain.model.TipoDocumento;

import java.util.List;
import java.util.Optional;

public class PeopleUseCase {
    private final PeopleRepository repo;

    public PeopleUseCase(PeopleRepository repo) {
        this.repo = repo;
    }

    public People save(People e) {
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

    public List<People> listar() { return repo.findAll(); }

    public void eliminar(Long id) { repo.deleteById(id); }

    public List<People> buscar(PeopleType peopleType, TipoDocumento tipoDocumento,
                               String numeroDocumento, String nombre, String apellido,
                               String codigo) {
        return repo.search(peopleType, tipoDocumento, numeroDocumento, nombre, apellido, codigo);
    }

    public People asignarEpc(Long empleadoId, String epc) {
        People e = repo.findById(empleadoId)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        if (e.getEpc() != null && !e.getEpc().isBlank()) {
            throw new IllegalStateException("Empleado ya tiene un EPC asignado");
        }

        e.setEpc(epc);
        return repo.save(e);
    }

    public Optional<People> findByEpc(String epc) {
        return repo.findByEpc(epc);
    }

    public java.util.List<People> buscarBasico(String nombre,
                                               String apellido,
                                               String numeroDocumento,
                                               String email) {
        return repo.findAll().stream()
                .filter(p -> likeContainsIgnoreCase(p.getFullName(), nombre))
                .filter(p -> likeContainsIgnoreCase(p.getLastName(), apellido))
                .filter(p -> likeContainsIgnoreCase(p.getDocNumber(), numeroDocumento))
                .filter(p -> likeContainsIgnoreCase(p.getEmail(), email))
                .toList();
    }

    private boolean likeContainsIgnoreCase(String value, String filtro) {
        if (filtro == null || filtro.isBlank()) {
            return true; // si no hay filtro, no limita
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.toLowerCase().contains(filtro.toLowerCase());
    }


}
