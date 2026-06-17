package be.atc.erpprojetintegration_1.enums;

public enum ContractType {
    PERMANENT("CDI"),
    FIXED_TERM("CDD"),
    TEMPORARY("Interim"),
    STUDENT("Etudiant"),
    REPLACEMENT("Remplacement");

    private final String label;

    ContractType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
