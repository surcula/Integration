package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.tools.MessageUtils;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@RequestScoped
public class AccessDeniedBean {

    /**
     * Adds an access denied message and redirects the user to the hub.
     *
     * @return JSF navigation outcome
     */
    public String redirectToHub() {
        MessageUtils.addWarningMessage("common.accessDenied");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/hub?faces-redirect=true";
    }
}
