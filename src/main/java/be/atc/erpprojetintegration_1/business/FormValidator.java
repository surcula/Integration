package be.atc.erpprojetintegration_1.business;

import java.util.Map;

public final class FormValidator {

    private FormValidator() {
    }

    /**
     * verify string is empty
     * @param input
     * @param fieldKey
     * @param errors
     * @return
     */
    public static void required(String input, String fieldKey, String messageKey, Map<String, String> errors) {
        if (input == null || input.trim().isEmpty()) {
            errors.put(fieldKey, messageKey);
        }
    }

    /**
     * verify maxLength and minLength
     * @param input
     * @param maxLength
     * @param minLength
     * @return
     */
    public static void lengthBetween(String input, String fieldKey, String messageKey,
                                     int minLength, int maxLength, Map<String, String> errors) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        int length = input.trim().length();

        if (length < minLength || length > maxLength) {
            errors.put(fieldKey, messageKey);
        }
    }
}
