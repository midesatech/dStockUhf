
package domain.usecase;
import domain.gateway.ProductoGateway;
import domain.model.Producto;
import java.util.List;
public class ProductoUseCase {
    private final ProductoGateway repo;
    public ProductoUseCase(ProductoGateway repo){ this.repo=repo; }
    public Producto crear(Producto p){ if(p==null) throw new IllegalArgumentException("Producto nulo"); return repo.save(p); }
    public List<Producto> listar(){ return repo.findAll(); }
    public void eliminar(Long id){ repo.deleteById(id); }
}
