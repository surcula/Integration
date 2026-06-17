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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
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
            List<Employee> employees = em.createNamedQuery("getActiveEmployeesByPlanning", Employee.class)
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
    public Result<List<PlanningsEmployee>> getActiveAssignments(Integer planningId) {
        EntityManager em = EMF.getEM();
        try {
            List<PlanningsEmployee> assignments = em.createNamedQuery("getActiveAssignmentsByPlanning", PlanningsEmployee.class)
                    .setParameter("planningId", planningId)
                    .getResultList();
            return Result.ok(assignments);
        } catch (Exception ex) {
            log.error("Error while loading assignments for planning", ex);
            return Result.fail(error("planning.assignments.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> updateAssignment(Integer assignmentId, String note, Boolean performed) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningsEmployee assignment = em.find(PlanningsEmployee.class, assignmentId);
            if (assignment == null) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.assignment.error.notFound"));
            }
            assignment.setNote(note != null ? note.trim() : null);
            assignment.setPerformed(performed != null ? performed : false);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while updating assignment", ex);
            return Result.fail(error("planning.assignment.error.save"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<PlanningsEmployee> getActiveAssignment(Integer planningId, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            List<PlanningsEmployee> assignments = em.createNamedQuery("getActiveAssignmentByPlanningAndEmployee", PlanningsEmployee.class)
                    .setParameter("planningId", planningId)
                    .setParameter("employeeId", employeeId)
                    .setParameter("cancelled", be.atc.erpprojetintegration_1.enums.PlanningStatus.CANCELLED)
                    .setMaxResults(1)
                    .getResultList();
            return Result.ok(assignments.isEmpty() ? null : assignments.get(0));
        } catch (Exception ex) {
            log.error("Error while loading an active planning assignment", ex);
            return Result.fail(error("planning.assignment.error.load"));
        } finally {
            em.close();
        }
    }





    @Override
    public Result<Void> replaceAssignments(Planning planning, List<Integer> employeeIds) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            Set<Integer> uniqueEmployeeIds = employeeIds == null
                    ? new LinkedHashSet<Integer>()
                    : new LinkedHashSet<>(employeeIds);

            List<PlanningsEmployee> activeAssignments = em.createNamedQuery("getActiveAssignmentsByPlanning", PlanningsEmployee.class)
                    .setParameter("planningId", planning.getId())
                    .getResultList();

            Set<Integer> retainedEmployeeIds = new LinkedHashSet<>();
            for (PlanningsEmployee activeAssignment : activeAssignments) {
                Integer activeEmployeeId = activeAssignment.getEmployee().getId();
                if (uniqueEmployeeIds.contains(activeEmployeeId)) {
                    retainedEmployeeIds.add(activeEmployeeId);
                    continue;
                }
                activeAssignment.setIsActive(false);
            }

            Planning managedPlanning = em.getReference(Planning.class, planning.getId());
            for (Integer employeeId : uniqueEmployeeIds) {
                if (employeeId == null || retainedEmployeeIds.contains(employeeId)) {
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
            LocalDate requestedEndDate = isOvernight(startHour, endHour) ? date.plusDays(1) : date;
            List<PlanningsEmployee> candidates = em.createNamedQuery("getCandidateAssignmentsForConflict", PlanningsEmployee.class)
                    .setParameter("employeeIds", employeeIds)
                    .setParameter("cancelled", be.atc.erpprojetintegration_1.enums.PlanningStatus.CANCELLED)
                    .setParameter("candidateStart", date.minusDays(1))
                    .setParameter("candidateEnd", requestedEndDate)
                    .getResultList();

            Map<Integer, Employee> conflicts = new java.util.LinkedHashMap<>();
            for (PlanningsEmployee candidate : candidates) {
                Planning existing = candidate.getPlanning();
                if (excludedPlanningId != null && excludedPlanningId.equals(existing.getId())) {
                    continue;
                }
                if (overlaps(date, startHour, endHour,
                        existing.getDate(), existing.getStartHour(), existing.getEndHour())) {
                    conflicts.put(candidate.getEmployee().getId(), candidate.getEmployee());
                }
            }
            return Result.ok(new ArrayList<>(conflicts.values()));

        } catch (Exception ex) {
            log.error("Error while detecting planning conflicts", ex);
            return Result.fail(error("planning.conflicts.error"));

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Employee>> findInsufficientRestEmployees(
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            List<Integer> employeeIds,
            Integer excludedPlanningId) {

        if (date == null || employeeIds == null || employeeIds.isEmpty() || startHour == null || endHour == null) {
            return Result.ok(new ArrayList<Employee>());
        }

        EntityManager em = EMF.getEM();
        try {
            LocalDate requestedEndDate = isOvernight(startHour, endHour) ? date.plusDays(1) : date;
            List<PlanningsEmployee> candidates = em.createNamedQuery(
                            "getCandidateServiceAssignmentsForWorkRules", PlanningsEmployee.class)
                    .setParameter("employeeIds", employeeIds)
                    .setParameter("cancelled", be.atc.erpprojetintegration_1.enums.PlanningStatus.CANCELLED)
                    .setParameter("candidateStart", date.minusDays(1))
                    .setParameter("candidateEnd", requestedEndDate.plusDays(1))
                    .getResultList();

            LocalDateTime requestedStart = date.atTime(startHour);
            LocalDateTime requestedEnd = date.plusDays(isOvernight(startHour, endHour) ? 1 : 0).atTime(endHour);
            Map<Integer, Employee> conflicts = new java.util.LinkedHashMap<>();
            for (PlanningsEmployee candidate : candidates) {
                Planning existing = candidate.getPlanning();
                if (excludedPlanningId != null && excludedPlanningId.equals(existing.getId())) continue;
                if (date.equals(existing.getDate())) continue;
                if (existing.getStartHour() == null || existing.getEndHour() == null) continue;

                LocalDateTime existingStart = existing.getDate().atTime(existing.getStartHour());
                LocalDateTime existingEnd = existing.getDate()
                        .plusDays(isOvernight(existing.getStartHour(), existing.getEndHour()) ? 1 : 0)
                        .atTime(existing.getEndHour());

                long restBefore = java.time.Duration.between(existingEnd, requestedStart).toHours();
                long restAfter = java.time.Duration.between(requestedEnd, existingStart).toHours();
                boolean existingBefore = !existingEnd.isAfter(requestedStart) && restBefore < 11;
                boolean existingAfter = !requestedEnd.isAfter(existingStart) && restAfter < 11;
                if (existingBefore || existingAfter) {
                    conflicts.put(candidate.getEmployee().getId(), candidate.getEmployee());
                }
            }
            return Result.ok(new ArrayList<>(conflicts.values()));
        } catch (Exception ex) {
            log.error("Error while detecting 11-hour rest violations", ex);
            return Result.fail(error("planning.rest.error"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Employee>> findExcessiveConsecutiveServiceEmployees(
            LocalDate date,
            List<Integer> employeeIds,
            Integer excludedPlanningId) {

        if (date == null || employeeIds == null || employeeIds.isEmpty()) {
            return Result.ok(new ArrayList<Employee>());
        }

        EntityManager em = EMF.getEM();
        try {
            List<PlanningsEmployee> candidates = em.createNamedQuery(
                            "getCandidateServiceAssignmentsForWorkRules", PlanningsEmployee.class)
                    .setParameter("employeeIds", employeeIds)
                    .setParameter("cancelled", be.atc.erpprojetintegration_1.enums.PlanningStatus.CANCELLED)
                    .setParameter("candidateStart", date.minusDays(7))
                    .setParameter("candidateEnd", date.plusDays(7))
                    .getResultList();

            Map<Integer, java.util.Set<LocalDate>> workedDaysByEmployee = new java.util.LinkedHashMap<>();
            Map<Integer, Employee> employeeById = new java.util.LinkedHashMap<>();
            for (Integer employeeId : employeeIds) {
                if (employeeId == null) continue;
                workedDaysByEmployee.put(employeeId, new java.util.TreeSet<LocalDate>());
            }
            for (PlanningsEmployee candidate : candidates) {
                Planning existing = candidate.getPlanning();
                if (excludedPlanningId != null && excludedPlanningId.equals(existing.getId())) continue;
                Integer employeeId = candidate.getEmployee().getId();
                java.util.Set<LocalDate> workedDays = workedDaysByEmployee.get(employeeId);
                if (workedDays == null) continue;
                workedDays.add(existing.getDate());
                employeeById.put(employeeId, candidate.getEmployee());
            }
            for (Integer employeeId : employeeIds) {
                if (employeeId != null) {
                    workedDaysByEmployee.computeIfAbsent(employeeId, k -> new java.util.TreeSet<LocalDate>()).add(date);
                }
            }

            Map<Integer, Employee> conflicts = new java.util.LinkedHashMap<>();
            for (Map.Entry<Integer, java.util.Set<LocalDate>> entry : workedDaysByEmployee.entrySet()) {
                int consecutive = 0;
                LocalDate previous = null;
                for (LocalDate workedDay : entry.getValue()) {
                    consecutive = previous != null && workedDay.equals(previous.plusDays(1)) ? consecutive + 1 : 1;
                    previous = workedDay;
                    if (consecutive > 7) {
                        Employee employee = employeeById.get(entry.getKey());
                        if (employee != null) conflicts.put(entry.getKey(), employee);
                        break;
                    }
                }
            }
            if (!conflicts.keySet().containsAll(employeeIds)) {
                for (PlanningsEmployee candidate : candidates) {
                    Integer employeeId = candidate.getEmployee().getId();
                    if (conflicts.containsKey(employeeId)) conflicts.put(employeeId, candidate.getEmployee());
                }
            }
            return Result.ok(new ArrayList<>(conflicts.values()));
        } catch (Exception ex) {
            log.error("Error while detecting consecutive service days", ex);
            return Result.fail(error("planning.consecutive.error"));
        } finally {
            em.close();
        }
    }

    private boolean isOvernight(LocalTime start, LocalTime end) {
        return start != null && end != null && end.isBefore(start);
    }

    private boolean overlaps(LocalDate leftDate, LocalTime leftStart, LocalTime leftEnd,
                             LocalDate rightDate, LocalTime rightStart, LocalTime rightEnd) {
        LocalDateTime leftStartDate = leftDate.atTime(leftStart == null ? LocalTime.MIN : leftStart);
        LocalDateTime leftEndDate = leftStart == null || leftEnd == null
                ? leftDate.plusDays(1).atStartOfDay()
                : leftDate.plusDays(isOvernight(leftStart, leftEnd) ? 1 : 0).atTime(leftEnd);
        LocalDateTime rightStartDate = rightDate.atTime(rightStart == null ? LocalTime.MIN : rightStart);
        LocalDateTime rightEndDate = rightStart == null || rightEnd == null
                ? rightDate.plusDays(1).atStartOfDay()
                : rightDate.plusDays(isOvernight(rightStart, rightEnd) ? 1 : 0).atTime(rightEnd);
        return leftStartDate.isBefore(rightEndDate) && leftEndDate.isAfter(rightStartDate);
    }

    private Map<String, String> error(String messageKey) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", messageKey);
        return errors;
    }
}
