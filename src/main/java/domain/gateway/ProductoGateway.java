
package domain.gateway;
import domain.model.Producto;
import java.util.List;
import java.util.Optional;
public interface ProductoGateway { Producto save(Producto p); List<Producto> findAll(); void deleteById(Long id); Optional<Producto> findById(Long id); }
