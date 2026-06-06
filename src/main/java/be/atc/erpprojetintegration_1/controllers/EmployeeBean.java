package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class EmployeeBean {
    private String email;
    private String password;

    @Inject
    private EmployeeBusiness employeeBusiness;
    @Inject
    private AuthBean authBean;
    public String login() {
        Result<ConnectedEmployeeDto> result = employeeBusiness.login(email, password);

        if (result == null || !result.isSuccess()) {
            MessageUtils.addErrorMessages(result,"login.error.invalid");
            return null;
        }
        authBean.connect(result.getData());
        return "hub?faces-redirect=true";
    }

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
}