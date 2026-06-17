package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;

@ApplicationScoped
public class EmployeeDepartmentServiceImpl implements IEmployeeDepartmentService {

    private static final Logger log = Logger.getLogger(EmployeeDepartmentServiceImpl.class);

    @Override
    public Result<EmployeeDepartment> getActiveEmployeeDepartmentByEmployeeId(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active employee department by employee id: " + id);
            EmployeeDepartment employeeDepartment = em
                    .createNamedQuery("getActiveEmployeeDepartmentByEmployeeId", EmployeeDepartment.class)
                    .setParameter("employeeId", id)
                    .getSingleResult();

            log.info("Active employee department found for employee id: " + id);
            return Result.ok(employeeDepartment);

        } catch (NoResultException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("notFound", "profile.error.department.notFound");

            log.warn("No active employee department found for employee id: " + id);
            return Result.fail(errors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching active employee department by employee id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<EmployeeDepartment>> getEmployeeList() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active employee departments");
            List<EmployeeDepartment> employeeDepartment = em.createNamedQuery("getAllActiveEmployeeDepartments", EmployeeDepartment.class)
                    .getResultList();

            log.info("Active employee departments found: " + employeeDepartment.size());
            return Result.ok(employeeDepartment);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching all active employee departments", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<EmployeeDepartment>> getAll() {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getAllEmployeeDepartments", EmployeeDepartment.class)
                    .getResultList());
        } catch (Exception ex) {
            log.error("Error while loading employee department history", ex);
            return Result.fail(error("message", "employeeDepartment.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<EmployeeDepartment> getById(Integer id) {
        EntityManager em = EMF.getEM();
        try {
            List<EmployeeDepartment> rows = em.createNamedQuery("getEmployeeDepartmentById", EmployeeDepartment.class)
                    .setParameter("id", id).getResultList();
            return rows.isEmpty()
                    ? Result.fail(error("notFound", "employeeDepartment.error.notFound"))
                    : Result.ok(rows.get(0));
        } catch (Exception ex) {
            log.error("Error while loading employee department", ex);
            return Result.fail(error("message", "employeeDepartment.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<EmployeeDepartment> assign(EmployeeDepartment assignment) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            LocalDate previousEndDate = assignment.getStartDate().minusDays(1);
            em.createNamedQuery("closeActiveEmployeeDepartments")
                    .setParameter("employeeId", assignment.getEmployee().getId())
                    .setParameter("endDate", previousEndDate)
                    .executeUpdate();
            assignment.setEmployee(em.getReference(be.atc.erpprojetintegration_1.entities.Employee.class,
                    assignment.getEmployee().getId()));
            assignment.setDepartment(em.getReference(be.atc.erpprojetintegration_1.entities.Department.class,
                    assignment.getDepartment().getId()));
            assignment.setIsActive(true);
            assignment.setEndDate(null);
            em.persist(assignment);
            em.getTransaction().commit();
            return Result.ok(assignment);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while assigning employee to department", ex);
            return Result.fail(error("message", "employeeDepartment.error.save"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> deactivate(Integer id, LocalDate endDate) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            EmployeeDepartment assignment = em.find(EmployeeDepartment.class, id);
            if (assignment == null) {
                em.getTransaction().rollback();
                return Result.fail(error("notFound", "employeeDepartment.error.notFound"));
            }
            assignment.setEndDate(endDate);
            assignment.setIsActive(false);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while deactivating employee department", ex);
            return Result.fail(error("message", "employeeDepartment.error.save"));
        } finally {
            em.close();
        }
    }

    private Map<String, String> error(String key, String value) {
        Map<String, String> errors = new HashMap<>();
        errors.put(key, value);
        return errors;
    }
}
