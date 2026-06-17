package be.atc.erpprojetintegration_1.enums;

public enum PlanningStatus {
    DRAFT("Brouillon"),
    PUBLISHED("Publie"),
    CANCELLED("Annule");

    private final String label;

    PlanningStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
