package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.log4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.UUID;

@Named
@SessionScoped
public class AuthBean implements Serializable {

    private static final Logger log = Logger.getLogger(AuthBean.class);

    private String email;
    private String password;
    private ConnectedEmployeeDto connectedEmployee;
    private String loginCelebrationToken;
    private String currentPassword;
    private String newPassword;
    private String passwordConfirmation;
    @Inject
    private EmployeeBusiness employeeBusiness;

    /**
     * Stores the authenticated employee in the session.
     *
     * @param employee connected employee dto
     */
    public void connect(ConnectedEmployeeDto employee) {
        this.connectedEmployee = employee;
        this.loginCelebrationToken = UUID.randomUUID().toString();
    }

    /**
     * Handles the login form submission and delegates authentication to Shiro.
     *
     * @return JSF navigation outcome
     */
    public String login() {
        log.info("Login attempt");

        Result<Void> validationResult = employeeBusiness.validateLoginForm(email, password);

        if (!validationResult.isSuccess()) {
            log.warn("Login form validation failed");
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
            password = null;

            log.info("Login successful for employee id: " + connectedEmployee.getId());
            if (Boolean.TRUE.equals(connectedEmployee.getMustChangePassword())) {
                log.info("Password change required for employee id: " + connectedEmployee.getId());
                return "/change-password?faces-redirect=true";
            }
            return "hub?faces-redirect=true";

        } catch (AuthenticationException ex) {
            log.warn("Login authentication failed");
            MessageUtils.addErrorMessage("login.error.invalid");
            return null;
        }
    }

    /**
     * Logs out the current user and clears session login data.
     *
     * @return JSF navigation outcome
     */
    public String logout() {
        Integer employeeId = connectedEmployee != null ? connectedEmployee.getId() : null;
        log.info("Logout requested for employee id: " + employeeId);

        SecurityUtils.getSubject().logout();

        connectedEmployee = null;
        loginCelebrationToken = null;
        email = null;
        password = null;
        currentPassword = null;
        newPassword = null;
        passwordConfirmation = null;

        return "/login?faces-redirect=true";
    }

    /**
     * Prevents access to the application while a password change is required.
     *
     * @return mandatory password page or no navigation outcome
     */
    public String enforcePasswordChange() {
        return connectedEmployee != null
                && Boolean.TRUE.equals(connectedEmployee.getMustChangePassword())
                ? "/change-password?faces-redirect=true"
                : null;
    }

    /**
     * Changes the connected employee password and opens the application.
     *
     * @return JSF navigation outcome
     */
    public String changePassword() {
        if (connectedEmployee == null) {
            return "/login?faces-redirect=true";
        }
        boolean mandatoryChange = Boolean.TRUE.equals(connectedEmployee.getMustChangePassword());

        Result<Void> result = employeeBusiness.changePassword(
                connectedEmployee.getId(), currentPassword, !mandatoryChange,
                newPassword, passwordConfirmation);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "password.change.error");
            return null;
        }

        connectedEmployee.setMustChangePassword(false);
        currentPassword = null;
        newPassword = null;
        passwordConfirmation = null;
        MessageUtils.addInfoMessage("password.change.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        log.info("Password changed for employee id: " + connectedEmployee.getId());
        return "/hub?faces-redirect=true";
    }

    public boolean isConnected() {
        return connectedEmployee != null;
    }

    public ConnectedEmployeeDto getConnectedEmployee() {
        return connectedEmployee;
    }

    public String getLoginCelebrationToken() {
        return loginCelebrationToken;
    }

    /**
     * Checks if the connected employee has the given role.
     *
     * @param roleName role name
     * @return true if the user has the role
     */
    public boolean hasRole(String roleName) {
        return connectedEmployee != null
                && connectedEmployee.getRoleName() != null
                && connectedEmployee.getRoleName().equals(roleName);
    }

    /**
     * Builds the full name displayed in the application layout.
     *
     * @return connected employee full name
     */
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

    /**
     * Checks if the connected employee is HR or admin.
     *
     * @return true if the connected employee is HR or admin
     */
    public boolean isHrOrAdmin() {
        return hasRole("HR") || hasRole("ADMIN");
    }

    /**
     * Checks if the connected employee has a Shiro permission.
     *
     * @param permissionName permission name
     * @return true if the user has the permission
     */
    public boolean hasPermission(String permissionName) {
        return SecurityUtils.getSubject().isPermitted(permissionName);
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public boolean isPasswordChangeRequired() {
        return connectedEmployee != null
                && Boolean.TRUE.equals(connectedEmployee.getMustChangePassword());
    }
}
