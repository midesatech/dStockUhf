package domain.model;

public enum TipoSangre {
    O_POS("O+"), O_NEG("O-"),
    A_POS("A+"), A_NEG("A-"),
    B_POS("B+"), B_NEG("B-"),
    AB_POS("AB+"), AB_NEG("AB-");

    private final String label;

    TipoSangre(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}