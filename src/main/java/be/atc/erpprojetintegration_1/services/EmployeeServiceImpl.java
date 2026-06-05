package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

@ApplicationScoped
public class EmployeeServiceImpl implements IEmployeeService {

    // Log4j
    private static final Logger log = Logger.getLogger(EmployeeServiceImpl.class);

    @Override
    public Result<Employee> login(String email, String password) {
        return null;
    }

    @Override
    public Result<Employee> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching employee by id: " + id);
            Employee employee = em.find(Employee.class, id);

            if (employee != null) {
                log.info("Employee found with id: " + id);
                return Result.ok(employee);
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("notFound", "Aucun employee trouvé avec l'ID " + id);
            log.warn("No employee found with id: " + id);
            return Result.fail(errors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            log.error("Error while searching employee by id: " + id, ex);
            return Result.fail(errors);
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Employee> getByEmail(String email) {
        return null;
    }

    @Override
    public Result<List<Employee>> getAllActive() {
        return null;
    }

    @Override
    public Result<List<Employee>> getAll() {
        return null;
    }

    @Override
    public Result<Employee> create(Employee employee) {
        return null;
    }

    @Override
    public Result<Employee> update(Employee employee) {
        return null;
    }

    @Override
    public Result<Void> deactivate(Integer id) {
        return null;
    }
}
