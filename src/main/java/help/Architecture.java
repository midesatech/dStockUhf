package help;

public enum Architecture {
    AMD64("amd64"),
    ARM("arm"),
    AARCH64("AARCH64");

    private final String value;

    Architecture(String value) {
        this.value = value;
    }

    public String getArch() {
        return this.value;
    }

}