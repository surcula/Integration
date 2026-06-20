package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.tools.MessageUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class AccessDeniedBean {

    @Inject
    private LanguageBean languageBean;

    /**
     * Adds an access denied message and redirects the user to the hub.
     *
     * @return JSF navigation outcome
     */
    public String redirectToHub() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getViewRoot().setLocale(languageBean.getLocale());
        String accessDeniedMessage = MessageUtils.getMessage("common.accessDenied");
        boolean messageAlreadyPresent = context.getMessageList().stream()
                .anyMatch(message -> accessDeniedMessage.equals(message.getSummary()));

        if (!messageAlreadyPresent) {
            MessageUtils.addWarningMessage("common.accessDenied");
        }

        context.getExternalContext().getFlash().setKeepMessages(true);
        return "/hub?faces-redirect=true";
    }
}
