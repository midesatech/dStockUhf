
package domain.gateway;

import domain.model.Category;
import domain.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductGateway {
    Product save(Product p);

    List<Product> findAll();

    void deleteById(Long id);

    Optional<Product> findById(Long id);

    List<Product> buscar(String sku, String nombre, Category cat);
    // 🔹 NUEVO: buscar equipo por EPC
    Optional<Product> findByEpc(String epc);
}
