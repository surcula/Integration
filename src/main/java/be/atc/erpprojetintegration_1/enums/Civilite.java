package be.atc.erpprojetintegration_1.enums;

public enum Civilite {
    MONSIEUR("MONSIEUR", "Monsieur"),
    MADAME("MADAME", "Madame"),
    MADEMOISELLE("MADEMOISELLE", "Mademoiselle"),
    DOCTEUR("DOCTEUR", "Docteur"),
    PROFESSEUR("PROFESSEUR", "Professeur");

    private final String code;
    private final String label;

    Civilite(String code, String label) {
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

        for (Civilite civilite : values()) {
            if (civilite.code.equals(code)) {
                return civilite.label;
            }
        }

        return "";
    }
}