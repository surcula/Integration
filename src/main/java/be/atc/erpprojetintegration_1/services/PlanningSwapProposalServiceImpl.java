package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.*;
import be.atc.erpprojetintegration_1.enums.PlanningStatus;
import be.atc.erpprojetintegration_1.enums.PlanningSwapProposalStatus;
import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;
import be.atc.erpprojetintegration_1.interfaces.IPlanningSwapProposalService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@ApplicationScoped
public class PlanningSwapProposalServiceImpl implements IPlanningSwapProposalService {
    private static final Logger log = Logger.getLogger(PlanningSwapProposalServiceImpl.class);

    @Override
    public Result<List<PlanningSwapProposal>> getByEmployee(Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getSwapProposalsByEmployee", PlanningSwapProposal.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("pending", PlanningSwapProposalStatus.PENDING)
                    .getResultList());
        } catch (Exception ex) {
            log.error("Error while loading swap proposals", ex);
            return Result.fail(error("planning.swap.proposal.error.load"));
        } finally { em.close(); }
    }

    @Override
    public Result<List<PlanningSwapProposal>> getByRequest(Integer requestId) {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getSwapProposalsByRequest", PlanningSwapProposal.class)
                    .setParameter("requestId", requestId).getResultList());
        } catch (Exception ex) {
            log.error("Error while loading request proposals", ex);
            return Result.fail(error("planning.swap.proposal.error.load"));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> propose(Integer requestId, List<Integer> employeeIds,
                                Integer proposedByEmployeeId, String comment) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningEmployeeSwapRequest request = em.find(PlanningEmployeeSwapRequest.class, requestId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (request == null || request.getStatus() != PlanningSwapStatus.PENDING) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.notPending"));
            }
            List<PlanningSwapProposal> existing = em.createNamedQuery(
                    "getSwapProposalsByRequest", PlanningSwapProposal.class)
                    .setParameter("requestId", requestId).getResultList();
            Map<Integer, PlanningSwapProposal> byEmployee = new HashMap<>();
            for (PlanningSwapProposal proposal : existing) {
                byEmployee.put(proposal.getProposedEmployee().getId(), proposal);
            }
            LocalDateTime now = LocalDateTime.now();
            for (Integer employeeId : new LinkedHashSet<>(employeeIds)) {
                PlanningSwapProposal proposal = byEmployee.get(employeeId);
                if (proposal == null) {
                    proposal = new PlanningSwapProposal();
                    proposal.setSwapRequest(request);
                    proposal.setProposedEmployee(em.getReference(Employee.class, employeeId));
                    proposal.setProposedBy(em.getReference(Employee.class, proposedByEmployeeId));
                    proposal.setIsActive(true);
                    proposal.setStatus(PlanningSwapProposalStatus.PENDING);
                    proposal.setProposedAt(now);
                    proposal.setRespondedAt(null);
                    em.persist(proposal);
                } else {
                    proposal.setStatus(PlanningSwapProposalStatus.PENDING);
                    proposal.setProposedAt(now);
                    proposal.setRespondedAt(null);
                }
            }
            request.setReviewComment(comment);
            request.setReviewedBy(em.getReference(Employee.class, proposedByEmployeeId));
            request.setRelaunchRequired(false);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while proposing swap replacements", ex);
            return Result.fail(error("planning.swap.proposal.error.save"));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> accept(Integer proposalId, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningSwapProposal proposal = em.find(PlanningSwapProposal.class, proposalId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (!canRespond(proposal, employeeId)) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.proposal.error.unavailable"));
            }
            PlanningEmployeeSwapRequest request = em.find(PlanningEmployeeSwapRequest.class,
                    proposal.getSwapRequest().getId(), LockModeType.PESSIMISTIC_WRITE);
            if (request.getStatus() != PlanningSwapStatus.PENDING || deadlinePassed(request)) {
                proposal.setStatus(PlanningSwapProposalStatus.EXPIRED);
                proposal.setRespondedAt(LocalDateTime.now());
                request.setRelaunchRequired(true);
                em.getTransaction().commit();
                return Result.fail(error("planning.swap.proposal.error.deadline"));
            }
            PlanningsEmployee current = request.getPlanningEmployee();
            if (!Boolean.TRUE.equals(current.getIsActive())) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.assignment"));
            }
            List<PlanningsEmployee> assigned = em.createNamedQuery(
                    "getActiveAssignmentByPlanningAndEmployee", PlanningsEmployee.class)
                    .setParameter("planningId", current.getPlanning().getId())
                    .setParameter("employeeId", employeeId)
                    .setParameter("cancelled", PlanningStatus.CANCELLED)
                    .setMaxResults(1).getResultList();
            if (!assigned.isEmpty()) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.error.alreadyAssigned"));
            }
            LocalDateTime now = LocalDateTime.now();
            current.setIsActive(false);
            PlanningsEmployee replacement = new PlanningsEmployee();
            replacement.setPlanning(current.getPlanning());
            replacement.setEmployee(em.getReference(Employee.class, employeeId));
            replacement.setIsActive(true);
            replacement.setPerformed(false);
            em.persist(replacement);
            proposal.setStatus(PlanningSwapProposalStatus.ACCEPTED);
            proposal.setRespondedAt(now);
            request.setStatus(PlanningSwapStatus.APPROVED);
            request.setReplacementEmployee(em.getReference(Employee.class, employeeId));
            request.setReviewedAt(now);
            request.setRelaunchRequired(false);
            em.createNamedQuery("cancelOtherSwapProposals")
                    .setParameter("cancelled", PlanningSwapProposalStatus.CANCELLED)
                    .setParameter("pending", PlanningSwapProposalStatus.PENDING)
                    .setParameter("respondedAt", now)
                    .setParameter("requestId", request.getId())
                    .setParameter("proposalId", proposal.getId()).executeUpdate();
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while accepting swap proposal", ex);
            return Result.fail(error("planning.swap.proposal.error.accept"));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> decline(Integer proposalId, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PlanningSwapProposal proposal = em.find(PlanningSwapProposal.class, proposalId,
                    LockModeType.PESSIMISTIC_WRITE);
            if (!canRespond(proposal, employeeId)) {
                em.getTransaction().rollback();
                return Result.fail(error("planning.swap.proposal.error.unavailable"));
            }
            proposal.setStatus(PlanningSwapProposalStatus.DECLINED);
            proposal.setRespondedAt(LocalDateTime.now());
            markRelaunchIfNoPending(em, proposal.getSwapRequest());
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while declining swap proposal", ex);
            return Result.fail(error("planning.swap.proposal.error.decline"));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> expireOverdue() {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            List<PlanningSwapProposal> proposals = em.createNamedQuery(
                    "getPendingSwapProposals", PlanningSwapProposal.class)
                    .setParameter("pending", PlanningSwapProposalStatus.PENDING).getResultList();
            Set<PlanningEmployeeSwapRequest> affected = new HashSet<>();
            for (PlanningSwapProposal proposal : proposals) {
                if (deadlinePassed(proposal.getSwapRequest())) {
                    proposal.setStatus(PlanningSwapProposalStatus.EXPIRED);
                    proposal.setRespondedAt(LocalDateTime.now());
                    affected.add(proposal.getSwapRequest());
                }
            }
            for (PlanningEmployeeSwapRequest request : affected) markRelaunchIfNoPending(em, request);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while expiring swap proposals", ex);
            return Result.fail(error("planning.swap.proposal.error.expire"));
        } finally { em.close(); }
    }

    private boolean canRespond(PlanningSwapProposal proposal, Integer employeeId) {
        return proposal != null && proposal.getStatus() == PlanningSwapProposalStatus.PENDING
                && proposal.getProposedEmployee().getId().equals(employeeId)
                && proposal.getSwapRequest().getStatus() == PlanningSwapStatus.PENDING;
    }

    private boolean deadlinePassed(PlanningEmployeeSwapRequest request) {
        Planning planning = request.getPlanningEmployee().getPlanning();
        LocalTime start = planning.getStartHour() == null ? LocalTime.MIN : planning.getStartHour();
        LocalDateTime planningStart = planning.getDate().atTime(start);
        LocalDateTime deadline = Boolean.TRUE.equals(request.getEmergencyMode())
                ? planningStart : planningStart.minusHours(24);
        return !LocalDateTime.now().isBefore(deadline);
    }

    private void markRelaunchIfNoPending(EntityManager em, PlanningEmployeeSwapRequest request) {
        Long pending = em.createNamedQuery("countPendingSwapProposalsByRequest", Long.class)
                .setParameter("requestId", request.getId())
                .setParameter("pending", PlanningSwapProposalStatus.PENDING).getSingleResult();
        request.setRelaunchRequired(pending == 0 && request.getStatus() == PlanningSwapStatus.PENDING);
    }

    private Map<String, String> error(String key) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", key);
        return errors;
    }
}
