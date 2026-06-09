package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DepartmentServiceImpl implements IDepartmentService {

    private static final Logger log = Logger.getLogger(DepartmentServiceImpl.class);

    @Override
    public Result<List<Department>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all departments");
            List<Department> departments = em.createNamedQuery("getAllDepartments", Department.class).getResultList();
            log.info("Departments found: " + departments.size());
            return Result.ok(departments);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "departments.error.load");
            log.error("Error while searching all departments", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Department> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching department by id: " + id);
            Department department = em.find(Department.class, id);

            if (department == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "departments.error.notFound");
                log.warn("No department found with id: " + id);
                return Result.fail(errors);
            }

            log.info("Department found with id: " + id);
            return Result.ok(department);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "departments.error.load");
            log.error("Error while searching department by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Department> create(Department department) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating department");
            em.getTransaction().begin();
            em.persist(department);
            em.getTransaction().commit();
            log.info("Department created with id: " + department.getId());
            return Result.ok(department);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "departments.error.save");
            log.error("Error while creating department", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Department> update(Department department) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating department id: " + department.getId());
            em.getTransaction().begin();
            Department updatedDepartment = em.merge(department);
            em.getTransaction().commit();
            log.info("Department updated with id: " + updatedDepartment.getId());
            return Result.ok(updatedDepartment);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "departments.error.save");
            log.error("Error while updating department id: " + department.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating department active status. Id: " + id + ", active: " + active);
            em.getTransaction().begin();
            Department department = em.find(Department.class, id);

            if (department == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "departments.error.notFound");
                log.warn("Cannot update department active status. Department not found with id: " + id);
                return Result.fail(errors);
            }

            department.setIsActive(active);
            em.merge(department);
            em.getTransaction().commit();
            log.info("Department active status updated. Id: " + id + ", active: " + active);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", active ? "departments.activate.error" : "departments.delete.error");
            log.error("Error while updating department active status. Id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
