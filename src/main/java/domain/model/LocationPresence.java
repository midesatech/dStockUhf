package domain.model;

public class LocationPresence {
    private final long locationId;
    private final String locationName;
    private final int employees;
    private final int equipment;


    public LocationPresence(long locationId, String locationName, int employees, int equipment) {
        this.locationId = locationId;
        this.locationName = locationName;
        this.employees = employees;
        this.equipment = equipment;
    }
    public long getLocationId() { return locationId; }
    public String getLocationName() { return locationName; }
    public int getEmployees() { return employees; }
    public int getEquipment() { return equipment; }
}
