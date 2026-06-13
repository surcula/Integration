package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.interfaces.IPlanningEmployeeService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class PlanningEmployeeServiceImpl implements IPlanningEmployeeService {

    private static final Logger log = Logger.getLogger(PlanningEmployeeServiceImpl.class);

    @Override
    public Result<List<Employee>> getActiveEmployees(Integer planningId) {
        EntityManager em = EMF.getEM();

        try {
            List<Employee> employees = em.createQuery(
                            "SELECT pe.employee FROM PlanningsEmployee pe " +
                                    "WHERE pe.planning.id = :planningId AND pe.isActive = true " +
                                    "ORDER BY pe.employee.lastName, pe.employee.firstName",
                            Employee.class)
                    .setParameter("planningId", planningId)
                    .getResultList();
            return Result.ok(employees);

        } catch (Exception ex) {
            log.error("Error while loading planning employee assignments", ex);
            return Result.fail(error("planning.assignments.error.load"));

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> replaceAssignments(Planning planning, List<Integer> employeeIds) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();

            em.createQuery(
                            "UPDATE PlanningsEmployee pe SET pe.isActive = false " +
                                    "WHERE pe.planning.id = :planningId AND pe.isActive = true")
                    .setParameter("planningId", planning.getId())
                    .executeUpdate();

            Set<Integer> uniqueEmployeeIds = employeeIds == null
                    ? new LinkedHashSet<Integer>()
                    : new LinkedHashSet<>(employeeIds);

            Planning managedPlanning = em.getReference(Planning.class, planning.getId());
            for (Integer employeeId : uniqueEmployeeIds) {
                if (employeeId == null) {
                    continue;
                }

                PlanningsEmployee assignment = new PlanningsEmployee();
                assignment.setPlanning(managedPlanning);
                assignment.setEmployee(em.getReference(Employee.class, employeeId));
                assignment.setIsActive(true);
                em.persist(assignment);
            }

            em.getTransaction().commit();
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.error("Error while replacing planning employee assignments", ex);
            return Result.fail(error("planning.assignments.error.save"));

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Employee>> findConflictingEmployees(
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            List<Integer> employeeIds,
            Integer excludedPlanningId) {

        if (date == null || employeeIds == null || employeeIds.isEmpty()) {
            return Result.ok(new ArrayList<Employee>());
        }

        EntityManager em = EMF.getEM();

        try {
            String query = "SELECT DISTINCT pe.employee FROM PlanningsEmployee pe " +
                    "JOIN pe.planning p " +
                    "WHERE pe.isActive = true AND p.isActive = true " +
                    "AND pe.employee.id IN :employeeIds " +
                    "AND p.date = :date ";

            boolean allDay = startHour == null && endHour == null;
            if (!allDay) {
                query += "AND (" +
                        "(p.startHour IS NULL AND p.endHour IS NULL) " +
                        "OR (p.startHour IS NOT NULL AND p.endHour IS NOT NULL " +
                        "AND p.startHour < :endHour AND p.endHour > :startHour)" +
                        ") ";
            }

            if (excludedPlanningId != null) {
                query += "AND p.id <> :excludedPlanningId ";
            }

            query += "ORDER BY pe.employee.lastName, pe.employee.firstName";

            javax.persistence.TypedQuery<Employee> typedQuery = em.createQuery(query, Employee.class)
                    .setParameter("employeeIds", employeeIds)
                    .setParameter("date", date);

            if (!allDay) {
                typedQuery.setParameter("startHour", startHour)
                        .setParameter("endHour", endHour);
            }

            if (excludedPlanningId != null) {
                typedQuery.setParameter("excludedPlanningId", excludedPlanningId);
            }

            return Result.ok(typedQuery.getResultList());

        } catch (Exception ex) {
            log.error("Error while detecting planning conflicts", ex);
            return Result.fail(error("planning.conflicts.error"));

        } finally {
            em.close();
        }
    }

    private Map<String, String> error(String messageKey) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", messageKey);
        return errors;
    }
}
