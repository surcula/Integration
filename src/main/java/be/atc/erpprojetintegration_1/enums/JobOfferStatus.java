package be.atc.erpprojetintegration_1.enums;

/**
 * Represents the business status of a job offer.
 * The status is used for the publication workflow, while isActive is kept for logical deletion.
 */
public enum JobOfferStatus {
    NOT_PUBLISHED("NOT_PUBLISHED", "Non publiee"),
    PUBLISHED("PUBLISHED", "Publiee"),
    ARCHIVED("ARCHIVED", "Archivee"),
    DELETED("DELETED", "Supprimee");

    private final String code;
    private final String label;

    JobOfferStatus(String code, String label) {
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

        for (JobOfferStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }

        return "";
    }
}
