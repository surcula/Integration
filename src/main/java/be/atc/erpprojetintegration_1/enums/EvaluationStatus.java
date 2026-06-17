package be.atc.erpprojetintegration_1.enums;

public enum EvaluationStatus {
    CREATED("CREATED", "Creee"),
    IN_PROGRESS("IN_PROGRESS", "En cours"),
    COMPLETED("COMPLETED", "Completee"),
    SCORE_CALCULATED("SCORE_CALCULATED", "Score calcule"),
    VALIDATED("VALIDATED", "Validee");

    private final String code;
    private final String label;

    EvaluationStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static String getLabelByCode(String code) {
        if (code == null) {
            return "";
        }

        for (EvaluationStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }

        return "";
    }
}
