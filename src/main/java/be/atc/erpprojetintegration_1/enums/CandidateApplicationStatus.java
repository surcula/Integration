package be.atc.erpprojetintegration_1.enums;

/**
 * Représente le statut métier d'une candidature pour une offre d'emploi.
 */
public enum CandidateApplicationStatus {
    RECEIVED("RECEIVED", "Reçue"),
    UNDER_REVIEW("UNDER_REVIEW", "En analyse"),
    INTERVIEW("INTERVIEW", "Entretien"),
    ACCEPTED("ACCEPTED", "Acceptée"),
    REJECTED("REJECTED", "Refusée"),
    ARCHIVED("ARCHIVED", "Archivée");

    private final String code;
    private final String label;

    CandidateApplicationStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
