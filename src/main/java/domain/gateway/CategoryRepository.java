
package domain.gateway;
import domain.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Category save(Category c);
    List<Category> findAll();
    void deleteById(Long id);
    Optional<Category> findById(Long id);
    Category update(Category c);
}
