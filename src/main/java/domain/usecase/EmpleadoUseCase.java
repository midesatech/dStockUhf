
package domain.usecase;
import domain.gateway.EmpleadoGateway;
import domain.model.Empleado;
import java.util.List;
public class EmpleadoUseCase {
    private final EmpleadoGateway repo;
    public EmpleadoUseCase(EmpleadoGateway repo){ this.repo=repo; }
    public Empleado crear(String codigo, String nombre){ if(nombre==null||nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido"); Empleado e = new Empleado(null,codigo,nombre.trim()); return repo.save(e); }
    public List<Empleado> listar(){ return repo.findAll(); }
    public void eliminar(Long id){ repo.deleteById(id); }
}
