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
}
