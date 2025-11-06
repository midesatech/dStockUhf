
package domain.usecase;

import domain.gateway.ProductGateway;
import domain.model.Category;
import domain.model.Product;

import java.util.List;
import java.util.Optional;

public class ProductUseCase {
    private final ProductGateway repo;

    public ProductUseCase(ProductGateway repo) {
        this.repo = repo;
    }

    public Product crear(Product p) {
        if (p == null) throw new IllegalArgumentException("Equipo nulo");
        return repo.save(p);
    }

    public List<Product> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public Product actualizar(Product p) {
        if (p == null) throw new IllegalArgumentException("Equipo nulo");
        return repo.save(p);
    }

    public List<Product> buscar(String sku, String nombre, Category cat) {
        return repo.buscar(sku, nombre, cat);
    }

    // 🔹 NUEVO: asignar EPC
    public Product asignarEpc(Long equipoId, String epc) {
        Product eq = repo.findById(equipoId)
                .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));

        if (eq.getEpc() != null && !eq.getEpc().isBlank()) {
            throw new IllegalStateException("Equipo ya tiene un EPC asignado");
        }

        eq.setEpc(epc);
        return repo.save(eq);
    }

    public Optional<Product> findByEpc(String epc) {
        return repo.findByEpc(epc);
    }
}
