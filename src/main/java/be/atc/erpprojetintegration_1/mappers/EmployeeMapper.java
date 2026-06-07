package be.atc.erpprojetintegration_1.mappers;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.enums.Civilite;
import be.atc.erpprojetintegration_1.enums.Gender;

public final class EmployeeMapper {

    private EmployeeMapper() {
    }

    /**
     * Map Employee into connectedEmployeeDto
     *
     * @param employee
     * @return
     */
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

    /**
     * MappEmployeeDepartment into EmployeeProfileDto
     *
     * @param employeeDepartment
     * @return
     */
    public static EmployeeProfileDto toEmployeeProfileDto(EmployeeDepartment employeeDepartment) {

        EmployeeProfileDto employeeProfileDto = new EmployeeProfileDto();
        Employee employee = employeeDepartment.getEmployee();
        Department department = employeeDepartment.getDepartment();

        if (employee != null) {
            employeeProfileDto.setFirstName(employee.getFirstName());
            employeeProfileDto.setLastName(employee.getLastName());
            employeeProfileDto.setEmail(employee.getEmail());
            employeeProfileDto.setPhone(employee.getPhone());
            employeeProfileDto.setBirthDate(employee.getBirthDate());
            employeeProfileDto.setPlaceOfBirth(employee.getPlaceOfBirth());
            employeeProfileDto.setCivilite(employee.getCivilite() != null ? employee.getCivilite().getLabel() : "");
            employeeProfileDto.setGender(employee.getGender() != null ? employee.getGender().getLabel() : "");
            employeeProfileDto.setEmployeeNumber(employee.getEmployeeNumber());

            Address address = employee.getAddress();

            if (address != null) {
                employeeProfileDto.setStreetName(address.getStreetName());
                employeeProfileDto.setStreetNumber(address.getStreetNumber());
                employeeProfileDto.setBoxNumber(address.getBoxNumber());

                City city = address.getCity();

                if (city != null) {
                    employeeProfileDto.setCityName(city.getCityName());
                    employeeProfileDto.setZipCode(city.getZipCode());
                }
            }
        }
        if (department != null) {
            employeeProfileDto.setDepartmentName(department.getDepartmentName());
        }
        return employeeProfileDto;
    }


    /**
     * Map EmployeeDepartment into EmployeeListDto.
     *
     * @param employeeDepartment employee department relation
     * @return employee list dto
     */
    public static EmployeeListDto toEmployeeListDto(EmployeeDepartment employeeDepartment) {
        if (employeeDepartment == null) {
            return null;
        }

        Employee employee = employeeDepartment.getEmployee();
        Department department = employeeDepartment.getDepartment();

        EmployeeListDto dto = new EmployeeListDto();

        if (employee != null) {
            dto.setId(employee.getId());
            dto.setFullName(employee.getFirstName() + " " + employee.getLastName());
            dto.setPhone(employee.getPhone());
            dto.setEmail(employee.getEmail());
        }

        if (department != null) {
            dto.setDepartmentName(department.getDepartmentName());
        }

        return dto;
    }

    /**
     * Map Employee into EmployeeListDto.
     *
     * @param employee employee entity
     * @return employee list dto
     */
    public static EmployeeListDto toEmployeeListDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeListDto dto = new EmployeeListDto();

        dto.setId(employee.getId());
        dto.setFullName(employee.getFirstName() + " " + employee.getLastName());
        dto.setPhone(employee.getPhone());
        dto.setEmail(employee.getEmail());

        if (employee.getEmployeeDepartments() != null) {
            for (EmployeeDepartment employeeDepartment : employee.getEmployeeDepartments()) {
                if (Boolean.TRUE.equals(employeeDepartment.getIsActive())
                        && employeeDepartment.getDepartment() != null
                        && Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive())) {
                    dto.setDepartmentName(employeeDepartment.getDepartment().getDepartmentName());
                    break;
                }
            }
        }

        return dto;
    }
}
