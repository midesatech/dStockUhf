
package domain.gateway;

import domain.model.People;
import domain.model.PeopleType;
import domain.model.TipoDocumento;

import java.util.List;
import java.util.Optional;

public interface PeopleRepository {
    People save(People e);

    List<People> findAll();

    void deleteById(Long id);

    Optional<People> findById(Long id);

    List<People> search(PeopleType peopleType, TipoDocumento tipoDocumento,
                        String numeroDocumento, String nombre, String apellido, String codigo);

    Optional<People> findByEpc(String epc);
}
