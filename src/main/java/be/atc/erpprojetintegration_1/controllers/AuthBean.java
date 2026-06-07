package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class AuthBean implements Serializable {


    private String email;
    private String password;
    private ConnectedEmployeeDto connectedEmployee;
    @Inject
    private EmployeeBusiness employeeBusiness;


    public void connect(ConnectedEmployeeDto employee) {
        this.connectedEmployee = employee;
    }

    public String login() {

        Result<Void> validationResult = employeeBusiness.validateLoginForm(email, password);

        if (!validationResult.isSuccess()) {
            MessageUtils.addErrorMessages(validationResult, "login.error.invalid");
            return null;
        }
        try {
            Subject currentUser = SecurityUtils.getSubject();

            UsernamePasswordToken token = new UsernamePasswordToken(email, password);

            currentUser.login(token);
            ConnectedEmployeeDto connectedEmployee =
                    (ConnectedEmployeeDto) currentUser.getPrincipal();

            connect(connectedEmployee);

            return "hub?faces-redirect=true";

        } catch (AuthenticationException ex) {
            MessageUtils.addErrorMessage("login.error.invalid");
            return null;
        }
    }

    public String logout() {
        SecurityUtils.getSubject().logout();

        connectedEmployee = null;
        email = null;
        password = null;

        return "/login?faces-redirect=true";
    }

    public boolean isConnected() {
        return connectedEmployee != null;
    }

    public ConnectedEmployeeDto getConnectedEmployee() {
        return connectedEmployee;
    }

    public boolean hasRole(String roleName) {
        return connectedEmployee != null
                && connectedEmployee.getRoleName() != null
                && connectedEmployee.getRoleName().equals(roleName);
    }

    public String getConnectedEmployeeFullName() {
        if (connectedEmployee == null) {
            return "";
        }

        String firstName = connectedEmployee.getFirstName() != null ? connectedEmployee.getFirstName() : "";
        String lastName = connectedEmployee.getLastName() != null ? connectedEmployee.getLastName() : "";

        return (firstName + " " + lastName).trim();
    }
    public String getConnectedEmployeeRoleName() {
        if (connectedEmployee == null || connectedEmployee.getRoleName() == null) {
            return "";
        }

        return connectedEmployee.getRoleName();
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
