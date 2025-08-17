
package domain.gateway;
import domain.model.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    Categoria save(Categoria c);
    List<Categoria> findAll();
    void deleteById(Long id);
    Optional<Categoria> findById(Long id);
    Categoria update(Categoria c);
}
