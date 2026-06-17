package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DepartmentHeadServiceImpl implements IDepartmentHeadService {

    private static final Logger log = Logger.getLogger(DepartmentHeadServiceImpl.class);

    @Override
    public Result<List<DepartmentHead>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active department heads");
            List<DepartmentHead> departmentHeads = em.createNamedQuery("getAllActiveDepartmentHeads", DepartmentHead.class)
                    .getResultList();
            log.info("Active department heads found: " + departmentHeads.size());
            return Result.ok(departmentHeads);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching active department heads", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching department head by id: " + id);
            DepartmentHead departmentHead = em.find(DepartmentHead.class, id);

            if (departmentHead == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "superiors.departmentHead.notFound");
                log.warn("No department head found with id: " + id);
                return Result.fail(errors);
            }

            departmentHead.getDepartment().getDepartmentName();
            departmentHead.getSuperior().getFirstName();
            log.info("Department head found with id: " + id);
            return Result.ok(departmentHead);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching department head by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> getActiveBySuperiorAndDepartment(Integer superiorId, Integer departmentId) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active department head. Superior id: " + superiorId + ", department id: " + departmentId);
            DepartmentHead departmentHead = em.createNamedQuery("getActiveDepartmentHeadBySuperiorAndDepartment", DepartmentHead.class)
                    .setParameter("superiorId", superiorId)
                    .setParameter("departmentId", departmentId)
                    .getSingleResult();

            log.info("Active department head found with id: " + departmentHead.getId());
            return Result.ok(departmentHead);

        } catch (NoResultException ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("notFound", "superiors.departmentHead.notFound");
            log.warn("No active department head found. Superior id: " + superiorId + ", department id: " + departmentId);
            return Result.fail(errors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching active department head", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> create(DepartmentHead departmentHead) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating department head");
            em.getTransaction().begin();
            Department department = em.find(Department.class, departmentHead.getDepartment().getId());
            Employee superior = em.find(Employee.class, departmentHead.getSuperior().getId());

            departmentHead.setDepartment(department);
            departmentHead.setSuperior(superior);
            em.persist(departmentHead);
            em.getTransaction().commit();
            log.info("Department head created with id: " + departmentHead.getId());
            return Result.ok(departmentHead);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.departmentHead.save.error");
            log.error("Error while creating department head", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> deactivate(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Deactivating department head id: " + id);
            em.getTransaction().begin();
            DepartmentHead departmentHead = em.find(DepartmentHead.class, id);

            if (departmentHead == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "superiors.departmentHead.notFound");
                log.warn("Cannot deactivate department head. Department head not found with id: " + id);
                return Result.fail(errors);
            }

            departmentHead.setIsActive(false);
            departmentHead.setEndDate(LocalDate.now());
            em.merge(departmentHead);
            em.getTransaction().commit();
            log.info("Department head deactivated with id: " + id);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.departmentHead.delete.error");
            log.error("Error while deactivating department head id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
