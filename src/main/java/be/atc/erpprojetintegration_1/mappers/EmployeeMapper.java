package be.atc.erpprojetintegration_1.mappers;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
import be.atc.erpprojetintegration_1.dto.AddressEditDto;
import be.atc.erpprojetintegration_1.dto.EmployeeEditDto;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.enums.Civilite;
import be.atc.erpprojetintegration_1.enums.EmploymentStatus;
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
            dto.setActive(employee.getIsActive());
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
        dto.setActive(employee.getIsActive());

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

    /**
     * Map Employee into EmployeeEditDto.
     *
     * @param employee employee entity
     * @return employee edit dto
     */
    public static EmployeeEditDto toEmployeeEditDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeEditDto dto = new EmployeeEditDto();

        dto.setId(employee.getId());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setPlaceOfBirth(employee.getPlaceOfBirth());
        dto.setCivilite(employee.getCivilite() != null ? employee.getCivilite().getCode() : null);
        dto.setGender(employee.getGender() != null ? employee.getGender().getCode() : null);
        dto.setEmploymentStatus(employee.getEmploymentStatus() != null ? employee.getEmploymentStatus().getCode() : null);
        dto.setEmployeeNumber(employee.getEmployeeNumber());
        dto.setAddress(toAddressEditDto(employee.getAddress()));

        return dto;
    }

    /**
     * Map Address into AddressEditDto.
     *
     * @param address address entity
     * @return address edit dto
     */
    public static AddressEditDto toAddressEditDto(Address address) {
        AddressEditDto dto = new AddressEditDto();

        if (address == null) {
            return dto;
        }

        dto.setId(address.getId());
        dto.setStreetName(address.getStreetName());
        dto.setStreetNumber(address.getStreetNumber());
        dto.setBoxNumber(address.getBoxNumber());

        if (address.getCity() != null) {
            dto.setCityId(address.getCity().getId());
        }

        return dto;
    }

    /**
     * Apply editable basic employee fields to an existing Employee entity.
     *
     * @param dto employee edit dto
     * @param employee employee entity to update
     */
    public static void updateEmployeeFromEditDto(EmployeeEditDto dto, Employee employee) {
        if (dto == null || employee == null) {
            return;
        }

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setPlaceOfBirth(dto.getPlaceOfBirth());
        employee.setEmploymentStatus(dto.getEmploymentStatus() != null && !dto.getEmploymentStatus().trim().isEmpty()
                ? EmploymentStatus.valueOf(dto.getEmploymentStatus())
                : null);
        employee.setEmployeeNumber(dto.getEmployeeNumber());
        employee.setCivilite(dto.getCivilite() != null && !dto.getCivilite().trim().isEmpty()
                ? Civilite.valueOf(dto.getCivilite())
                : null);
        employee.setGender(dto.getGender() != null && !dto.getGender().trim().isEmpty()
                ? Gender.valueOf(dto.getGender())
                : null);
    }

    /**
     * Map EmployeeEditDto into a new Employee entity.
     *
     * @param dto employee edit dto
     * @return employee entity
     */
    public static Employee toEmployee(EmployeeEditDto dto) {
        if (dto == null) {
            return null;
        }

        Employee employee = new Employee();
        updateEmployeeFromEditDto(dto, employee);
        employee.setIsActive(true);

        return employee;
    }

    /**
     * Apply editable address fields to an Address entity.
     *
     * @param dto address edit dto
     * @param address address entity
     * @param city city entity
     */
    public static void updateAddressFromEditDto(AddressEditDto dto, Address address, City city) {
        if (dto == null || address == null) {
            return;
        }

        address.setStreetName(dto.getStreetName());
        address.setStreetNumber(dto.getStreetNumber());
        address.setBoxNumber(dto.getBoxNumber());
        address.setCity(city);
        address.setIsActive(true);
    }
}
