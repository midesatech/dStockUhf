
package domain.usecase;

import domain.gateway.EquipmentGateway;
import domain.model.Categoria;
import domain.model.Equipment;

import java.util.List;

public class EquipmentUseCase {
    private final EquipmentGateway repo;

    public EquipmentUseCase(EquipmentGateway repo) {
        this.repo = repo;
    }

    public Equipment crear(Equipment p) {
        if (p == null) throw new IllegalArgumentException("Equipo nulo");
        return repo.save(p);
    }

    public List<Equipment> listar() {
        return repo.findAll();
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public Equipment actualizar(Equipment p) {
        if (p == null) throw new IllegalArgumentException("Equipo nulo");
        return repo.save(p);
    }

    public List<Equipment> buscar(String sku, String nombre, Categoria cat) {
        return repo.buscar(sku, nombre, cat);
    }
}
