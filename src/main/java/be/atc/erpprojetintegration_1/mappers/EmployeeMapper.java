package be.atc.erpprojetintegration_1.mappers;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Role;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    public static ConnectedEmployeeDto toConnectedEmployeeDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        ConnectedEmployeeDto dto = new ConnectedEmployeeDto();

        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());

        Role role = employee.getRole();

        if (role != null) {
            dto.setRoleId(role.getId());
            dto.setRoleName(role.getRoleName());
        }

        return dto;
    }
}