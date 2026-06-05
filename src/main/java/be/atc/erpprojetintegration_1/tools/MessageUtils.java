package be.atc.erpprojetintegration_1.tools;

import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

/**
 * Utility class used to retrieve localized messages from the JSF resource bundle.
 * <p>
 * This class centralizes access to the application's message files, such as
 * {@code messages_fr.properties} and {@code messages_en.properties}.
 * It uses the current JSF view locale to return the message in the active language.
 */
public final class MessageUtils {

    private static final String BUNDLE_NAME = "be.atc.messages.messages";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MessageUtils() {
    }

    /**
     * Retrieves a localized message from the application's resource bundle.
     *
     * @param key the key of the message to retrieve
     * @return the localized message matching the given key and current JSF locale
     */
    public static String getMessage(String key) {
        FacesContext context = FacesContext.getCurrentInstance();

        ResourceBundle bundle = ResourceBundle.getBundle(
                BUNDLE_NAME,
                context.getViewRoot().getLocale()
        );

        return bundle.getString(key);
    }
}