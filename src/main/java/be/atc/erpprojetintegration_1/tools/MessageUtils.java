package be.atc.erpprojetintegration_1.tools;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

public final class MessageUtils {

    private static final String BUNDLE_NAME = "be.atc.messages.messages";

    private MessageUtils() {
    }

    public static String getMessage(String key) {
        FacesContext context = FacesContext.getCurrentInstance();

        ResourceBundle bundle = ResourceBundle.getBundle(
                BUNDLE_NAME,
                context.getViewRoot().getLocale()
        );

        return bundle.getString(key);
    }

    public static void addErrorMessages(Result<?> result, String defaultMessageKey) {
        if (result == null || result.getErrors() == null || result.getErrors().isEmpty()) {
            addErrorMessage(defaultMessageKey);
            return;
        }

        for (String messageKey : result.getErrors().values()) {
            addErrorMessage(messageKey);
        }
    }

    public static void addErrorMessage(String messageKey) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        getMessage(messageKey),
                        null));
    }

    public static void addInfoMessage(String messageKey) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        getMessage(messageKey),
                        null));
    }
}