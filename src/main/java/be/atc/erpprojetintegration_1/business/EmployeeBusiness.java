package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.mappers.EmployeeMapper;
import be.atc.erpprojetintegration_1.tools.Result;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmployeeBusiness {

    @Inject
    private IEmployeeService employeeService;
    @Inject
    private IEmployeeDepartmentService employeeDepartment;


    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }


    /**
     * Validate email and password
     *
     * @param email
     * @param password
     * @return
     */
    public Result<Void> validateLoginForm(String email, String password) {
        Map<String, String> errors = new HashMap<>();

        FormValidator.required(email, "email", "login.email.required", errors);
        FormValidator.required(password, "password", "login.password.required", errors);
        FormValidator.lengthBetween(email, "email", "login.email.length", 5, 150, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    /**
     * Search profile with department.
     *
     * @param employeeId employee id
     * @return profile result
     */
    public Result<EmployeeProfileDto> getProfile(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "profile.error.employee.required");
            return Result.fail(errors);
        }

        Result<EmployeeDepartment> employeDepartementResult =
                employeeDepartment.getActiveEmployeeDepartmentByEmployeeId(employeeId);


        if (!employeDepartementResult.isSuccess()) {
            return Result.fail(employeDepartementResult.getErrors());
        }
        EmployeeProfileDto profileDto = EmployeeMapper.toEmployeeProfileDto(employeDepartementResult.getData());

        return Result.ok(profileDto);
    }

    public Result<List<EmployeeListDto>> getEmployeeList() {
        Result<List<Employee>> result = employeeService.getAllActiveWithDepartments();

        if (!result.isSuccess()) {
            return Result.fail(result.getErrors());
        }

        List<EmployeeListDto> employees = result.getData()
                .stream()
                .map(EmployeeMapper::toEmployeeListDto)
                .collect(Collectors.toList());

        return Result.ok(employees);
    }
}
