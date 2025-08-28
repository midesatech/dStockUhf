package domain.usecase;

import domain.gateway.DashboardRepository;
import domain.model.LocationPresence;
import domain.model.Occupant;

import java.time.LocalDateTime;
import java.util.List;

public class DashboardUseCase {
    private final DashboardRepository repo;

    public DashboardUseCase(DashboardRepository repo) {
        this.repo = repo;
    }

    public List<LocationPresence> getPresenceSince(LocalDateTime since) {
        return repo.fetchPresenceByLocationSince(since);
    }

    public List<Occupant> getOccupantsByUbicacion(long ubicacionId) {
        return repo.fetchOccupantsByUbicacion(ubicacionId);
    }
}
