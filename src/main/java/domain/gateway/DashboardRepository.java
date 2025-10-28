package domain.gateway;

import domain.model.LocationPresence;
import domain.model.Occupant;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository {
    // Presencia agregada por ubicación principal (suma principal + sub)
    List<LocationPresence> fetchPresenceByPrincipalSince(LocalDateTime since);

    // Breakdown por sububicación de una principal
    List<LocationPresence> fetchPresenceBySubOf(long principalId, LocalDateTime since);

    // Ocupantes (personas/equipos) por ubicación (acepta principal o sub)
    List<Occupant> fetchOccupantsByUbicacion(long ubicacionId);

    int totalEmployees();
    int totalEquipment();
}
