package be.atc.erpprojetintegration_1.seeding;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.services.EmployeeServiceImpl;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EmployeeStart {

    // Log4j
    private static final Logger log = Logger.getLogger(EmployeeStart.class);
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
        log.info("Ok l'admin n'existe pas.");
        Employee employee = new Employee();
        employee.setLastName("Admin");
        employee.setFirstName("Default");
        employee.setEmail(email);
        employee.setPassword(hashPassword("Admin"));
        employee.setEmployeeNumber("ADMIN-001");
        employee.setIsActive(true);

        log.info("Création de l'admin");
        employeeService.create(employee);

    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
}