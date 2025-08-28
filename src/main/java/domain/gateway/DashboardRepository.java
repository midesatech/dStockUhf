package domain.gateway;

import domain.model.LocationPresence;
import domain.model.Occupant;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardRepository {
    List<LocationPresence> fetchPresenceByLocationSince(LocalDateTime since);
    List<Occupant> fetchOccupantsByUbicacion(long ubicacionId);
}
