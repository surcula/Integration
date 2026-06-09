package be.atc.erpprojetintegration_1.enums;

public enum EmploymentStatus {
    EMPLOYEE("EMPLOYEE", "Employe"),
    WORKER("WORKER", "Ouvrier"),
    CONTRACTUAL("CONTRACTUAL", "Contractuel"),
    STATUTORY("STATUTORY", "Statutaire"),
    TEMPORARY("TEMPORARY", "Interimaire"),
    STUDENT("STUDENT", "Etudiant"),
    INTERN("INTERN", "Stagiaire"),
    APPRENTICE("APPRENTICE", "Apprenti"),
    SELF_EMPLOYED("SELF_EMPLOYED", "Independant"),
    VOLUNTEER("VOLUNTEER", "Volontaire");

    private final String code;
    private final String label;

    EmploymentStatus(String code, String label) {
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

        for (EmploymentStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }

        return "";
    }
}
