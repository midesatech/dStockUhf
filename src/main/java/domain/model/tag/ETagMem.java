package domain.model.tag;

public enum ETagMem {

    EPC(0x01),
    TID(0x02),
    USR(0x03); // Added USR with a value

    private final int value;

    ETagMem(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) value; // Cast to byte when retrieving
    }

    public static ETagMem fromValue(int value) {
        for (ETagMem tag : ETagMem.values()) {
            if (tag.getValue() == value) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Invalid ETagMem value: " + value);
    }
}
