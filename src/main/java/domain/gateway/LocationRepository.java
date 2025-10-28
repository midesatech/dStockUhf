
package domain.gateway;

import domain.model.Ubicacion;

import java.util.List;
import java.util.Optional;

public interface LocationRepository {
    Ubicacion save(Ubicacion u);

    List<Ubicacion> findAll();

    List<Ubicacion> findPrincipals();           // parentId = null
    List<Ubicacion> findByParentId(Long parentId); // sububicaciones de un principal

    void deleteById(Long id);

    Optional<Ubicacion> findById(Long id);
}
