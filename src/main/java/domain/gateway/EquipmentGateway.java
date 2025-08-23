
package domain.gateway;

import domain.model.Categoria;
import domain.model.Equipment;

import java.util.List;
import java.util.Optional;

public interface EquipmentGateway {
    Equipment save(Equipment p);

    List<Equipment> findAll();

    void deleteById(Long id);

    Optional<Equipment> findById(Long id);

    List<Equipment> buscar(String sku, String nombre, Categoria cat);
    // 🔹 NUEVO: buscar equipo por EPC
    Optional<Equipment> findByEpc(String epc);
}
