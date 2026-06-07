package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

@ApplicationScoped
public class EmployeeServiceImpl implements IEmployeeService {

    // Log4j
    private static final Logger log = Logger.getLogger(EmployeeServiceImpl.class);


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
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching employee by email: " + email);

            Employee employee = em.createNamedQuery("getEmployeeByEmail", Employee.class)
                    .setParameter("email", email)
                    .getSingleResult();

            log.info("Employee found with email: " + email);
            return Result.ok(employee);

        } catch (NoResultException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("notFound", "Aucun employee trouvé avec l'email " + email);
            log.warn("No employee found with email: " + email);
            return Result.fail(errors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            log.error("Error while searching employee by email: " + email, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Employee>> getAllActive() {
        return null;
    }

    @Override
    public Result<List<Employee>> getAllActiveWithDepartments() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active employees with departments");

            List<Employee> employees = em
                    .createNamedQuery("getAllActiveEmployeesWithDepartments", Employee.class)
                    .getResultList();

            log.info("Active employees found: " + employees.size());
            return Result.ok(employees);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching all active employees with departments", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Employee>> getAll() {
        return null;
    }

    @Override
    public Result<Employee> create(Employee employee) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating employee");

            em.getTransaction().begin();
            em.persist(employee);
            em.getTransaction().commit();

            log.info("Employee created with id: " + employee.getId());
            return Result.ok(employee);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while creating employee", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
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
