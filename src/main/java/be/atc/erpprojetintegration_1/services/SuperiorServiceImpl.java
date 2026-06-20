package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Superior;
import be.atc.erpprojetintegration_1.interfaces.ISuperiorService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SuperiorServiceImpl implements ISuperiorService {

    private static final Logger log = Logger.getLogger(SuperiorServiceImpl.class);

    @Override
    public Result<List<Superior>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all superior assignments");
            List<Superior> superiors = em.createNamedQuery("getAllSuperiors", Superior.class).getResultList();
            log.info("Superior assignments found: " + superiors.size());
            return Result.ok(superiors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching all superior assignments", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Superior>> getActiveBySuperiorId(Integer superiorId) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active superior assignments by superior id: " + superiorId);
            List<Superior> superiors = em.createNamedQuery("getActiveSuperiorsBySuperiorId", Superior.class)
                    .setParameter("superiorId", superiorId)
                    .getResultList();
            log.info("Active superior assignments found: " + superiors.size());
            return Result.ok(superiors);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching active superior assignments by superior id: " + superiorId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Superior> getActiveByEmployeeId(Integer employeeId) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active superior assignment by employee id: " + employeeId);
            List<Superior> assignments = em.createNamedQuery(
                            "getActiveSuperiorByEmployeeId", Superior.class)
                    .setParameter("employeeId", employeeId)
                    .setMaxResults(1)
                    .getResultList();

            if (assignments.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "superiors.error.notFound");
                return Result.fail(errors);
            }

            return Result.ok(assignments.get(0));

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching active superior assignment by employee id: "
                    + employeeId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Map<String, Integer>> getManagedEmployeeCountsBySuperiorAndDepartment() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Counting active managed employees by superior and department");

            List<Object[]> rows = em.createQuery(
                            "SELECT s.superior.id, ed.department.id, COUNT(s.id) " +
                                    "FROM Superior s " +
                                    "JOIN s.employee e " +
                                    "JOIN s.superior sup " +
                                    "JOIN e.employeeDepartments ed " +
                                    "JOIN ed.department d " +
                                    "WHERE s.isActive = true " +
                                    "AND e.isActive = true " +
                                    "AND sup.isActive = true " +
                                    "AND ed.isActive = true " +
                                    "AND d.isActive = true " +
                                    "GROUP BY s.superior.id, ed.department.id",
                            Object[].class)
                    .getResultList();

            Map<String, Integer> counts = new HashMap<>();

            for (Object[] row : rows) {
                Integer superiorId = (Integer) row[0];
                Integer departmentId = (Integer) row[1];
                Long count = (Long) row[2];
                counts.put(buildSuperiorDepartmentKey(superiorId, departmentId), count.intValue());
            }

            log.info("Managed employee counts found: " + counts.size());
            return Result.ok(counts);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while counting active managed employees by superior and department", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> deactivateTeam(Integer superiorEmployeeId, Integer departmentId) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Deactivating team for superior id: " + superiorEmployeeId + ", department id: " + departmentId);

            em.getTransaction().begin();
            int updatedRows = em.createQuery(
                            "UPDATE Superior s " +
                                    "SET s.isActive = false " +
                                    "WHERE s.superior.id = :superiorEmployeeId " +
                                    "AND s.isActive = true " +
                                    "AND s.employee.id IN (" +
                                    "   SELECT ed.employee.id " +
                                    "   FROM EmployeeDepartment ed " +
                                    "   WHERE ed.department.id = :departmentId " +
                                    "   AND ed.isActive = true" +
                                    ")")
                    .setParameter("superiorEmployeeId", superiorEmployeeId)
                    .setParameter("departmentId", departmentId)
                    .executeUpdate();
            em.getTransaction().commit();

            log.info("Team deactivated. Updated superior assignments: " + updatedRows);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.team.delete.error");
            log.error("Error while deactivating team for superior id: " + superiorEmployeeId
                    + ", department id: " + departmentId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Superior> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching superior assignment by id: " + id);
            Superior superior = em.find(Superior.class, id);

            if (superior == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "superiors.error.notFound");
                log.warn("No superior assignment found with id: " + id);
                return Result.fail(errors);
            }

            superior.getEmployee().getFirstName();
            superior.getSuperior().getFirstName();
            log.info("Superior assignment found with id: " + id);
            return Result.ok(superior);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.load");
            log.error("Error while searching superior assignment by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Superior> create(Superior superior) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating superior assignment");
            em.getTransaction().begin();
            em.persist(superior);
            em.getTransaction().commit();
            log.info("Superior assignment created with id: " + superior.getId());
            return Result.ok(superior);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.save");
            log.error("Error while creating superior assignment", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Superior> update(Superior superior) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating superior assignment id: " + superior.getId());
            em.getTransaction().begin();
            Superior updatedSuperior = em.merge(superior);
            em.getTransaction().commit();
            log.info("Superior assignment updated with id: " + updatedSuperior.getId());
            return Result.ok(updatedSuperior);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "superiors.error.save");
            log.error("Error while updating superior assignment id: " + superior.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating superior assignment active status. Id: " + id + ", active: " + active);
            em.getTransaction().begin();
            Superior superior = em.find(Superior.class, id);

            if (superior == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "superiors.error.notFound");
                log.warn("Cannot update superior assignment active status. Superior assignment not found with id: " + id);
                return Result.fail(errors);
            }

            superior.setIsActive(active);
            em.merge(superior);
            em.getTransaction().commit();
            log.info("Superior assignment active status updated. Id: " + id + ", active: " + active);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", active ? "superiors.activate.error" : "superiors.delete.error");
            log.error("Error while updating superior assignment active status. Id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    private String buildSuperiorDepartmentKey(Integer superiorId, Integer departmentId) {
        return String.valueOf(superiorId) + ":" + String.valueOf(departmentId);
    }
}
