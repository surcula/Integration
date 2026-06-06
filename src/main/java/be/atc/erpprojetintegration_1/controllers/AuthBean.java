package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class AuthBean implements Serializable {

    private ConnectedEmployeeDto connectedEmployee;

    public void connect(ConnectedEmployeeDto employee) {
        this.connectedEmployee = employee;
    }

    public void logout() {
        this.connectedEmployee = null;
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
}
