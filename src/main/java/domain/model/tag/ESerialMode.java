package domain.model.tag;

public enum ESerialMode {
    NO(0x00),
    BB(0x01),
    CF(0x03);

    private final int value;

    ESerialMode(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) value; // Cast to byte when retrieving
    }

    public static ESerialMode fromValue(int value) {
        for (ESerialMode mode : ESerialMode.values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Invalid ESerialMode value: " + value);
    }
}
