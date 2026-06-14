package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PlanningEmployeeSwapRequest;
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;
import be.atc.erpprojetintegration_1.interfaces.IPlanningEmployeeService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
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
    public Result<PlanningsEmployee> getActiveAssignment(Integer planningId, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            List<PlanningsEmployee> assignments = em.createQuery(
                            "SELECT pe FROM PlanningsEmployee pe " +
                                    "JOIN FETCH pe.planning p JOIN FETCH pe.employee e " +
                                    "LEFT JOIN FETCH p.department " +
                                    "WHERE p.id = :planningId AND e.id = :employeeId " +
                                    "AND pe.isActive = true AND p.isActive = true",
                            PlanningsEmployee.class)
                    .setParameter("planningId", planningId)
                    .setParameter("employeeId", employeeId)
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
    public Result<List<PlanningEmployeeSwapRequest>> getSwapRequests(Integer employeeId, boolean allEmployees) {
        EntityManager em = EMF.getEM();
        try {
            String jpql = "SELECT DISTINCT sr FROM PlanningEmployeeSwapRequest sr " +
                    "JOIN FETCH sr.planningEmployee pe JOIN FETCH pe.planning p " +
                    "JOIN FETCH pe.employee assigned JOIN FETCH sr.requestedBy requester " +
                    "LEFT JOIN FETCH p.department " +
                    "LEFT JOIN FETCH sr.replacementEmployee replacement " +
                    "LEFT JOIN FETCH sr.reviewedBy reviewer " +
                    "WHERE sr.isActive = true ";
            if (!allEmployees) {
                jpql += "AND requester.id = :employeeId ";
            }
            jpql += "ORDER BY CASE WHEN sr.status = :pending THEN 0 ELSE 1 END, sr.requestedAt DESC";
            javax.persistence.TypedQuery<PlanningEmployeeSwapRequest> query = em.createQuery(
                    jpql, PlanningEmployeeSwapRequest.class).setParameter("pending", PlanningSwapStatus.PENDING);
            if (!allEmployees) {
                query.setParameter("employeeId", employeeId);
            }
            return Result.ok(query.getResultList());
        } catch (Exception ex) {
            log.error("Error while loading planning swap requests", ex);
            return Result.fail(error("planning.swap.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<PlanningEmployeeSwapRequest> getSwapRequest(Integer requestId) {
        EntityManager em = EMF.getEM();
        try {
            List<PlanningEmployeeSwapRequest> requests = em.createQuery(
                            "SELECT sr FROM PlanningEmployeeSwapRequest sr " +
                                    "JOIN FETCH sr.planningEmployee pe JOIN FETCH pe.planning p " +
                                    "JOIN FETCH pe.employee JOIN FETCH sr.requestedBy " +
                                    "LEFT JOIN FETCH p.department LEFT JOIN FETCH sr.replacementEmployee " +
                                    "LEFT JOIN FETCH sr.reviewedBy WHERE sr.id = :requestId AND sr.isActive = true",
                            PlanningEmployeeSwapRequest.class)
                    .setParameter("requestId", requestId)
                    .setMaxResults(1)
                    .getResultList();
            return Result.ok(requests.isEmpty() ? null : requests.get(0));
        } catch (Exception ex) {
            log.error("Error while loading a planning swap request", ex);
            return Result.fail(error("planning.swap.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<PlanningEmployeeSwapRequest> createSwapRequest(
            Integer planningEmployeeId, Integer requestedByEmployeeId, String reason) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningsEmployee assignment = em.find(PlanningsEmployee.class, planningEmployeeId);
            if (assignment == null || !Boolean.TRUE.equals(assignment.getIsActive())
                    || !assignment.getEmployee().getId().equals(requestedByEmployeeId)) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.access"));
            }
            Long pending = em.createQuery(
                            "SELECT COUNT(sr) FROM PlanningEmployeeSwapRequest sr " +
                                    "WHERE sr.planningEmployee.id = :assignmentId " +
                                    "AND sr.status = :status AND sr.isActive = true", Long.class)
                    .setParameter("assignmentId", planningEmployeeId)
                    .setParameter("status", PlanningSwapStatus.PENDING)
                    .getSingleResult();
            if (pending > 0) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.pending"));
            }
            PlanningEmployeeSwapRequest request = new PlanningEmployeeSwapRequest();
            request.setPlanningEmployee(assignment);
            request.setRequestedBy(em.getReference(Employee.class, requestedByEmployeeId));
            request.setStatus(PlanningSwapStatus.PENDING);
            request.setReason(reason);
            request.setRequestedAt(LocalDateTime.now());
            request.setIsActive(true);
            em.persist(request);
            em.getTransaction().commit();
            return Result.ok(request);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while creating a planning swap request", ex);
            return Result.fail(error("planning.swap.error.save"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> reviewSwapRequest(Integer requestId, PlanningSwapStatus status,
                                          Integer replacementEmployeeId, Integer reviewedByEmployeeId,
                                          String reviewComment) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningEmployeeSwapRequest request = em.find(PlanningEmployeeSwapRequest.class, requestId);
            if (request == null || request.getStatus() != PlanningSwapStatus.PENDING) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.notPending"));
            }
            if (status == PlanningSwapStatus.APPROVED) {
                PlanningsEmployee current = request.getPlanningEmployee();
                if (!Boolean.TRUE.equals(current.getIsActive())) {
                    em.getTransaction().rollback();
                    return Result.fail(error("planning.swap.error.assignment"));
                }
                if (replacementEmployeeId == null || replacementEmployeeId.equals(current.getEmployee().getId())) {
                    em.getTransaction().rollback();
                    return Result.fail(error("planning.swap.error.replacement"));
                }
                current.setIsActive(false);
                PlanningsEmployee replacement = new PlanningsEmployee();
                replacement.setPlanning(current.getPlanning());
                replacement.setEmployee(em.getReference(Employee.class, replacementEmployeeId));
                replacement.setIsActive(true);
                em.persist(replacement);
                request.setReplacementEmployee(em.getReference(Employee.class, replacementEmployeeId));
            }
            request.setStatus(status);
            request.setReviewComment(reviewComment);
            request.setReviewedBy(em.getReference(Employee.class, reviewedByEmployeeId));
            request.setReviewedAt(LocalDateTime.now());
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while reviewing a planning swap request", ex);
            return Result.fail(error("planning.swap.error.review"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> cancelSwapRequest(Integer requestId, Integer requestedByEmployeeId) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningEmployeeSwapRequest request = em.find(PlanningEmployeeSwapRequest.class, requestId);
            if (request == null || request.getStatus() != PlanningSwapStatus.PENDING
                    || !request.getRequestedBy().getId().equals(requestedByEmployeeId)) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.access"));
            }
            request.setStatus(PlanningSwapStatus.CANCELLED);
            request.setReviewedAt(LocalDateTime.now());
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while cancelling a planning swap request", ex);
            return Result.fail(error("planning.swap.error.cancel"));
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

            List<PlanningsEmployee> activeAssignments = em.createQuery(
                            "SELECT pe FROM PlanningsEmployee pe JOIN FETCH pe.employee " +
                                    "WHERE pe.planning.id = :planningId AND pe.isActive = true",
                            PlanningsEmployee.class)
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
                em.createQuery(
                                "UPDATE PlanningEmployeeSwapRequest sr SET sr.status = :cancelled, " +
                                        "sr.reviewedAt = :reviewedAt WHERE sr.planningEmployee.id = :assignmentId " +
                                        "AND sr.status = :pending AND sr.isActive = true")
                        .setParameter("cancelled", PlanningSwapStatus.CANCELLED)
                        .setParameter("reviewedAt", LocalDateTime.now())
                        .setParameter("assignmentId", activeAssignment.getId())
                        .setParameter("pending", PlanningSwapStatus.PENDING)
                        .executeUpdate();
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
            List<PlanningsEmployee> candidates = em.createQuery(
                            "SELECT pe FROM PlanningsEmployee pe JOIN FETCH pe.employee e " +
                                    "JOIN FETCH pe.planning p WHERE pe.isActive = true AND p.isActive = true " +
                                    "AND e.id IN :employeeIds AND p.date BETWEEN :candidateStart AND :candidateEnd " +
                                    "ORDER BY e.lastName, e.firstName",
                            PlanningsEmployee.class)
                    .setParameter("employeeIds", employeeIds)
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
