package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.Result;
import org.mindrot.jbcrypt.BCrypt;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EmployeeBusiness {

    @Inject
    private IEmployeeService employeeService;

    public Result<Employee> login(String email, String password){
        Map<String, String> errors = new HashMap<>();
        errors.put("login", "Email ou mot de passe incorrect");

        return Result.fail(errors);
    }

    private boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }






}
