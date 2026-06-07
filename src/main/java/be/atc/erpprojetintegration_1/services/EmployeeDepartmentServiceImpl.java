package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmployeeDepartmentServiceImpl implements IEmployeeDepartmentService {

    @Override
    public Result<EmployeeDepartment> getActiveEmployeeDepartmentByEmployeeId(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            EmployeeDepartment employeeDepartment = em
                    .createNamedQuery("getActiveEmployeeDepartmentByEmployeeId", EmployeeDepartment.class)
                    .setParameter("employeeId", id)
                    .getSingleResult();

            return Result.ok(employeeDepartment);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<EmployeeDepartment>> getEmployeeList() {
        EntityManager em = EMF.getEM();

        try {
            List<EmployeeDepartment> employeeDepartment = em.createNamedQuery("getAllActiveEmployeeDepartments", EmployeeDepartment.class)
                    .getResultList();

            return Result.ok(employeeDepartment);

        } catch (NoResultException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("notFound", "profile.error.department.notFound");
            return Result.fail(errors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
