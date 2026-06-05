package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.application.FacesMessage;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;


@Named
@RequestScoped
public class EmployeeBean {
    private String email;
    private String password;

    @Inject
    private EmployeeBusiness employeeBusiness;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }



    public String login(){
        Result<Employee> result = employeeBusiness.login(email,password);

        if ( result == null || !result.isSuccess()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            MessageUtils.getMessage("login.error.invalid"),
                            null
                    ));
            return null;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();

        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Connexion réussie",
                        "Vous êtes connectée"));

        facesContext.getExternalContext().getFlash().setKeepMessages(true);

        return "hub?faces-redirect=true";

    }


}
