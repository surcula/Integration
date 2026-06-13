package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.interfaces.IPlanningService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PlanningServiceImpl implements IPlanningService {

    private static final Logger log = Logger.getLogger(PlanningServiceImpl.class);

    @Override
    public Result<List<Planning>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            List<Planning> plannings = em.createNamedQuery("getAllActivePlannings", Planning.class).getResultList();
            return Result.ok(plannings);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "planning.error.load");
            log.error("Error while loading plannings", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Planning>> getActiveByEmployee(Integer employeeId) {
        EntityManager em = EMF.getEM();

        try {
            List<Planning> plannings = em.createQuery(
                            "SELECT DISTINCT p FROM PlanningsEmployee pe " +
                                    "JOIN pe.planning p " +
                                    "LEFT JOIN FETCH p.department " +
                                    "WHERE pe.employee.id = :employeeId " +
                                    "AND pe.isActive = true AND p.isActive = true " +
                                    "ORDER BY p.date, p.startHour",
                            Planning.class)
                    .setParameter("employeeId", employeeId)
                    .getResultList();
            return Result.ok(plannings);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "planning.error.load");
            log.error("Error while loading plannings for employee id: " + employeeId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Planning> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            List<Planning> plannings = em.createQuery(
                            "SELECT p FROM Planning p LEFT JOIN FETCH p.department WHERE p.id = :planningId",
                            Planning.class)
                    .setParameter("planningId", id)
                    .getResultList();
            if (plannings.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "planning.error.notFound");
                return Result.fail(errors);
            }
            return Result.ok(plannings.get(0));

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "planning.error.load");
            log.error("Error while loading planning id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Planning> getByIdForEmployee(Integer id, Integer employeeId) {
        EntityManager em = EMF.getEM();

        try {
            List<Planning> plannings = em.createQuery(
                            "SELECT DISTINCT p FROM PlanningsEmployee pe " +
                                    "JOIN pe.planning p " +
                                    "LEFT JOIN FETCH p.department " +
                                    "WHERE p.id = :planningId AND pe.employee.id = :employeeId " +
                                    "AND pe.isActive = true AND p.isActive = true",
                            Planning.class)
                    .setParameter("planningId", id)
                    .setParameter("employeeId", employeeId)
                    .getResultList();

            if (plannings.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "planning.error.notFound");
                return Result.fail(errors);
            }
            return Result.ok(plannings.get(0));

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "planning.error.load");
            log.error("Error while loading planning for employee id: " + employeeId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Planning> save(Planning planning) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();

            Planning savedPlanning;
            if (planning.getId() == null) {
                em.persist(planning);
                savedPlanning = planning;
            } else {
                savedPlanning = em.merge(planning);
            }

            em.getTransaction().commit();
            return Result.ok(savedPlanning);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "planning.error.save");
            log.error("Error while saving planning", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            Planning planning = em.find(Planning.class, id);

            if (planning == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "planning.error.notFound");
                return Result.fail(errors);
            }

            planning.setIsActive(active);
            em.merge(planning);
            em.getTransaction().commit();
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "planning.error.save");
            log.error("Error while updating planning active status", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
