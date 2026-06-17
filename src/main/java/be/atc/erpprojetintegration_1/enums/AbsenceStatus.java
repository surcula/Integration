package be.atc.erpprojetintegration_1.enums;

public enum AbsenceStatus {
    DRAFT("Brouillon"),
    PENDING("En attente"),
    APPROVED("Approuvee"),
    REFUSED("Refusee"),
    CANCELLED("Annulee");

    private final String label;

    AbsenceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
