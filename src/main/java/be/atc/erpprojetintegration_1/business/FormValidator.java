package be.atc.erpprojetintegration_1.business;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
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

    /**
     * Verifies an IBAN format and check digits using the modulo 97 rule.
     *
     * @param input IBAN value
     * @param fieldKey field key
     * @param messageKey message key
     * @param errors error map
     */
    public static void iban(String input, String fieldKey, String messageKey, Map<String, String> errors) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        String iban = input.replaceAll("\\s+", "").toUpperCase();

        if (!iban.matches("[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}")) {
            errors.put(fieldKey, messageKey);
            return;
        }

        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;

        for (int i = 0; i < rearranged.length(); i++) {
            char current = rearranged.charAt(i);

            if (Character.isDigit(current)) {
                remainder = (remainder * 10 + Character.getNumericValue(current)) % 97;
            } else if (Character.isLetter(current)) {
                int value = Character.getNumericValue(current);
                remainder = (remainder * 100 + value) % 97;
            } else {
                errors.put(fieldKey, messageKey);
                return;
            }
        }

        if (remainder != 1) {
            errors.put(fieldKey, messageKey);
        }
    }

    /**
     * Verifies that a birth date produces an age between the given limits.
     *
     * @param input birth date in ISO format yyyy-MM-dd
     * @param fieldKey field key
     * @param messageKey message key
     * @param minAge minimum accepted age
     * @param maxAge maximum accepted age
     * @param errors error map
     */
    public static void ageBetween(String input, String fieldKey, String messageKey,
                                  int minAge, int maxAge, Map<String, String> errors) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        try {
            LocalDate birthDate = LocalDate.parse(input.trim());
            int age = Period.between(birthDate, LocalDate.now()).getYears();

            if (age < minAge || age > maxAge) {
                errors.put(fieldKey, messageKey);
            }
        } catch (DateTimeParseException ex) {
            errors.put(fieldKey, messageKey);
        }
    }
}
