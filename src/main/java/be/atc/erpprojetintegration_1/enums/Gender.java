package be.atc.erpprojetintegration_1.enums;

public enum Gender {
    FEMALE("FEMALE", "Femme"),
    MALE("MALE", "Homme"),
    OTHER("OTHER", "Autre");

    private final String code;
    private final String label;

    Gender(String code, String label) {
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

        for (Gender gender : values()) {
            if (gender.code.equals(code)) {
                return gender.label;
            }
        }

        return "";
    }
}
