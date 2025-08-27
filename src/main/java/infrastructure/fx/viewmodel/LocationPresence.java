package infrastructure.fx.viewmodel;

public class LocationPresence {
    private final Long locationId;
    private final String locationName;
    private final int employees;
    private final int equipment;

    public LocationPresence(Long locationId, String locationName, int employees, int equipment) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.employees = employees;
        this.equipment = equipment;
    }

    public Long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public int getEmployees() { return employees; }
    public int getEquipment() { return equipment; }
}
