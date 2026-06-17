package be.atc.erpprojetintegration_1.enums;

public enum ContractStatus {
    ACTIVE("Actif"),
    CLOSED("Cloture");

    private final String label;

    ContractStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
}
