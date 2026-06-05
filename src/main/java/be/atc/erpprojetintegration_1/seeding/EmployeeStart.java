package be.atc.erpprojetintegration_1.seeding;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.Result;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmployeeStart {

    @Inject
    private IEmployeeService employeeService;


    public void createDefaultAdminIfNotExists() {
        String email = "admin@test.be";

        Result<Employee> existingEmployee = employeeService.getByEmail(email);

        if (existingEmployee != null
                && existingEmployee.isSuccess()
                && existingEmployee.getData() != null) {
            return;
        }

        Employee employee = new Employee();
        employee.setLastName("Admin");
        employee.setFirstName("Default");
        employee.setEmail(email);
        employee.setPassword(hashPassword("Admin"));
        employee.setEmployeeNumber("ADMIN-001");
        employee.setIsActive(true);

        employeeService.create(employee);

    }
    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}