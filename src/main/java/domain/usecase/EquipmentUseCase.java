
package domain.usecase;

import domain.gateway.EquipmentGateway;
import domain.model.Category;
import domain.model.Equipment;

import java.util.List;
import java.util.Optional;

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

    public List<Equipment> buscar(String sku, String nombre, Category cat) {
        return repo.buscar(sku, nombre, cat);
    }

    // 🔹 NUEVO: asignar EPC
    public Equipment asignarEpc(Long equipoId, String epc) {
        Equipment eq = repo.findById(equipoId)
                .orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));

        if (eq.getEpc() != null && !eq.getEpc().isBlank()) {
            throw new IllegalStateException("Equipo ya tiene un EPC asignado");
        }

        eq.setEpc(epc);
        return repo.save(eq);
    }

    public Optional<Equipment> findByEpc(String epc) {
        return repo.findByEpc(epc);
    }
}
