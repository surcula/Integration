package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Absence;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.interfaces.IAbsenceService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AbsenceServiceImpl implements IAbsenceService {
    private static final Logger log = Logger.getLogger(AbsenceServiceImpl.class);

    public Result<List<Absence>> getAllActive() { return listNamed("getAllActiveAbsences", null); }
    public Result<List<Absence>> getActiveByEmployee(Integer employeeId) { return listNamed("getActiveAbsencesByEmployee", employeeId); }

    private Result<List<Absence>> listNamed(String query, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            javax.persistence.TypedQuery<Absence> typedQuery = em.createNamedQuery(query, Absence.class);
            if (employeeId != null) typedQuery.setParameter("employeeId", employeeId);
            return Result.ok(typedQuery.getResultList());
        } catch (Exception ex) {
            log.error("Error while loading absences", ex);
            return Result.fail(error("message", "absence.error.load"));
        } finally { em.close(); }
    }

    public Result<Absence> getById(Integer id) {
        EntityManager em = EMF.getEM();
        try {
            List<Absence> rows = em.createQuery("SELECT a FROM Absence a JOIN FETCH a.employee LEFT JOIN FETCH a.reviewer WHERE a.id = :id", Absence.class)
                    .setParameter("id", id).getResultList();
            return rows.isEmpty() ? Result.fail(error("notFound", "absence.error.notFound")) : Result.ok(rows.get(0));
        } catch (Exception ex) {
            log.error("Error while loading absence", ex);
            return Result.fail(error("message", "absence.error.load"));
        } finally { em.close(); }
    }

    public Result<Absence> save(Absence absence) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            Absence saved = absence.getId() == null ? absence : em.merge(absence);
            if (absence.getId() == null) em.persist(saved);
            em.getTransaction().commit();
            return Result.ok(saved);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while saving absence", ex);
            return Result.fail(error("message", "absence.error.save"));
        } finally { em.close(); }
    }

    public Result<List<Absence>> getBlockingAbsences(Integer employeeId, LocalDate startDate, LocalDate endDate, Integer excludedId) {
        EntityManager em = EMF.getEM();
        try {
            String jpql = "SELECT a FROM Absence a JOIN FETCH a.employee WHERE a.employee.id = :employeeId AND a.isActive = true " +
                    "AND a.status IN (be.atc.erpprojetintegration_1.enums.AbsenceStatus.PENDING, be.atc.erpprojetintegration_1.enums.AbsenceStatus.APPROVED) " +
                    "AND a.startDate <= :endDate AND a.endDate >= :startDate";
            if (excludedId != null) jpql += " AND a.id <> :excludedId";
            javax.persistence.TypedQuery<Absence> query = em.createQuery(jpql, Absence.class)
                    .setParameter("employeeId", employeeId).setParameter("startDate", startDate)
                    .setParameter("endDate", endDate);
            if (excludedId != null) query.setParameter("excludedId", excludedId);
            List<Absence> rows = query.getResultList();
            return Result.ok(rows);
        } catch (Exception ex) {
            log.error("Error while checking absence conflicts", ex);
            return Result.fail(error("message", "absence.error.conflict"));
        } finally { em.close(); }
    }

    public Result<List<Planning>> getEmployeePlannings(Integer employeeId, LocalDate startDate, LocalDate endDate) {
        EntityManager em = EMF.getEM();
        try {
            List<Planning> rows = em.createQuery("SELECT DISTINCT p FROM PlanningsEmployee pe JOIN pe.planning p WHERE pe.employee.id = :employeeId " +
                                    "AND pe.isActive = true AND p.isActive = true AND p.date BETWEEN :startDate AND :endDate", Planning.class)
                    .setParameter("employeeId", employeeId).setParameter("startDate", startDate.minusDays(1))
                    .setParameter("endDate", endDate).getResultList();
            return Result.ok(rows);
        } catch (Exception ex) {
            log.error("Error while checking planning conflicts", ex);
            return Result.fail(error("message", "absence.error.planningConflict"));
        } finally { em.close(); }
    }

    private Map<String, String> error(String key, String value) {
        Map<String, String> errors = new HashMap<>(); errors.put(key, value); return errors;
    }
}
