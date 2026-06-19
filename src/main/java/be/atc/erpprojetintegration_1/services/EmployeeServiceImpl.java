package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.LocalDate;
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
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active employees");

            List<Employee> employees = em
                    .createNamedQuery("getAllActiveEmployees", Employee.class)
                    .getResultList();

            log.info("Active employees found: " + employees.size());
            return Result.ok(employees);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching all active employees", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
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
    public Result<List<Employee>> getAllWithDepartments() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all employees with departments");

            List<Employee> employees = em
                    .createNamedQuery("getAllEmployeesWithDepartments", Employee.class)
                    .getResultList();

            log.info("Employees found: " + employees.size());
            return Result.ok(employees);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching all employees with departments", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Employee>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all employees");

            List<Employee> employees = em
                    .createNamedQuery("getAllEmployees", Employee.class)
                    .getResultList();

            log.info("Employees found: " + employees.size());
            return Result.ok(employees);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching all employees", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
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
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating employee with id: " + employee.getId());

            em.getTransaction().begin();
            Employee updatedEmployee = em.merge(employee);
            em.getTransaction().commit();

            log.info("Employee updated with id: " + updatedEmployee.getId());
            return Result.ok(updatedEmployee);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while updating employee", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> resetPassword(Integer id, String hashedPassword) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Resetting password for employee id: " + id);
            em.getTransaction().begin();

            Employee employee = em.find(Employee.class, id);
            if (employee == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "employee.resetPassword.error.notFound");
                log.warn("Password reset failed. Employee not found with id: " + id);
                return Result.fail(errors);
            }

            if (!Boolean.TRUE.equals(employee.getIsActive())) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("inactive", "employee.resetPassword.error.inactive");
                log.warn("Password reset refused for inactive employee id: " + id);
                return Result.fail(errors);
            }

            employee.setPassword(hashedPassword);
            employee.setMustChangePassword(true);
            em.getTransaction().commit();

            log.info("Password reset completed for employee id: " + id);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "employee.resetPassword.error");
            log.error("Error while resetting password for employee id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> deactivate(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Deactivating employee with id: " + id);

            em.getTransaction().begin();
            Employee employee = em.find(Employee.class, id);

            if (employee == null) {
                em.getTransaction().rollback();

                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "employee.delete.error.notFound");
                log.warn("No employee found with id: " + id);
                return Result.fail(errors);
            }

            LocalDate endDate = LocalDate.now();

            employee.setIsActive(false);
            em.merge(employee);

            int closedDepartmentAssignments = em.createQuery(
                            "UPDATE EmployeeDepartment ed " +
                                    "SET ed.isActive = false, ed.endDate = :endDate " +
                                    "WHERE ed.employee.id = :employeeId " +
                                    "AND ed.isActive = true")
                    .setParameter("endDate", endDate)
                    .setParameter("employeeId", id)
                    .executeUpdate();

            int closedSuperiorAssignments = em.createQuery(
                            "UPDATE Superior s " +
                                    "SET s.isActive = false, s.endDate = :endDate " +
                                    "WHERE (s.employee.id = :employeeId OR s.superior.id = :employeeId) " +
                                    "AND s.isActive = true")
                    .setParameter("endDate", endDate)
                    .setParameter("employeeId", id)
                    .executeUpdate();

            int closedDepartmentHeads = em.createQuery(
                            "UPDATE DepartmentHead dh " +
                                    "SET dh.isActive = false, dh.endDate = :endDate " +
                                    "WHERE dh.superior.id = :employeeId " +
                                    "AND dh.isActive = true")
                    .setParameter("endDate", endDate)
                    .setParameter("employeeId", id)
                    .executeUpdate();

            em.getTransaction().commit();

            log.info("Employee deactivated with id: " + id
                    + ". Closed department assignments: " + closedDepartmentAssignments
                    + ", superior assignments: " + closedSuperiorAssignments
                    + ", department head assignments: " + closedDepartmentHeads);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while deactivating employee with id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> activate(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Activating employee with id: " + id);

            em.getTransaction().begin();
            Employee employee = em.find(Employee.class, id);

            if (employee == null) {
                em.getTransaction().rollback();

                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "employee.activate.error.notFound");
                log.warn("No employee found with id: " + id);
                return Result.fail(errors);
            }

            employee.setIsActive(true);
            em.merge(employee);
            em.getTransaction().commit();

            log.info("Employee activated with id: " + id);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while activating employee with id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
