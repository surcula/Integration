package be.atc.erpprojetintegration_1.enums;

public enum EvaluationObjectiveStatus {
    TO_DO("TO_DO", "A faire"),
    IN_PROGRESS("IN_PROGRESS", "En cours"),
    ACHIEVED("ACHIEVED", "Atteint"),
    NOT_ACHIEVED("NOT_ACHIEVED", "Non atteint");

    private final String code;
    private final String label;

    EvaluationObjectiveStatus(String code, String label) {
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

        for (EvaluationObjectiveStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }

        return "";
    }
}
