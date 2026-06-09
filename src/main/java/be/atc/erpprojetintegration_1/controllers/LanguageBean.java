package be.atc.erpprojetintegration_1.controllers;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named
@SessionScoped
public class LanguageBean implements Serializable {
    private Locale locale = Locale.FRENCH;

    /**
     * Returns the current JSF locale.
     *
     * @return current locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Switches the current view language to French.
     *
     * @return JSF navigation outcome
     */
    public String setFrench() {
        locale = Locale.FRENCH;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        return null;
    }

    /**
     * Switches the current view language to English.
     *
     * @return JSF navigation outcome
     */
    public String setEnglish() {
        locale = Locale.ENGLISH;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        return null;
    }
}
