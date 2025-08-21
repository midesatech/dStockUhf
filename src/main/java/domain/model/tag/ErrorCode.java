package domain.model.tag;

public enum ErrorCode {
    OK(0x00),
    TM(0x02),
    TC(0x03),
    NC(0x04);

    private final int value;

    ErrorCode(int value) {
        this.value = value;
    }

    public byte getValue() {
        return (byte) value; // Cast to byte when retrieving
    }

    public static ErrorCode fromValue(int value) {
        for (ErrorCode tag : ErrorCode.values()) {
            if (tag.getValue() == value) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Invalid ETagMem value: " + value);
    }
}
