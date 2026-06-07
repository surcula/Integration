package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.dto.ConnectedEmployeeDto;
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


}
