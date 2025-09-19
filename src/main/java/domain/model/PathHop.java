package domain.model;

import java.time.LocalDateTime;

public class PathHop {
    private final Long locationId;
    private final String locationName;
    private final LocalDateTime firstSeen;
    private final LocalDateTime lastSeen;
    private final int count; // # lecturas en ese tramo

    public PathHop(Long locationId, String locationName, LocalDateTime firstSeen, LocalDateTime lastSeen, int count) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.firstSeen = firstSeen;
        this.lastSeen = lastSeen;
        this.count = count;
    }

    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public LocalDateTime getFirstSeen() { return firstSeen; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public int getCount() { return count; }
}
