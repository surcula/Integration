package be.atc.erpprojetintegration_1.dto;

import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.Role;

import java.time.LocalDate;

public class ConnectedEmployeeDto {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private Integer roleId;
    private String roleName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
