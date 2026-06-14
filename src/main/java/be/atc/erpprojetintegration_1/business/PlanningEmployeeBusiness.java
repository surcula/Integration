package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PlanningEmployeeSwapRequest;
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.entities.PlanningSwapProposal;
import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;
import be.atc.erpprojetintegration_1.interfaces.IPlanningEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.IPlanningSwapProposalService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PlanningEmployeeBusiness {

    @Inject
    private IPlanningEmployeeService planningEmployeeService;

    @Inject
    private IPlanningSwapProposalService planningSwapProposalService;

    @Inject
    private AbsenceBusiness absenceBusiness;

    public Result<List<PlanningsEmployee>> getActiveAssignments(Integer planningId) {
        if (planningId == null) {
            return Result.ok(Collections.<PlanningsEmployee>emptyList());
        }
        return planningEmployeeService.getActiveAssignments(planningId);
    }

    public Result<Void> updateAssignment(Integer assignmentId, String note, Boolean performed) {
        if (assignmentId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("assignmentId", "planning.assignment.error.notFound");
            return Result.fail(errors);
        }
        return planningEmployeeService.updateAssignment(assignmentId, note, performed);
    }

    public Result<PlanningsEmployee> getAssignment(Integer planningId, Integer employeeId) {
        if (planningId == null || employeeId == null) {
            return Result.fail(error("planning.swap.error.assignment"));
        }
        return planningEmployeeService.getActiveAssignment(planningId, employeeId);
    }

    public Result<List<PlanningEmployeeSwapRequest>> getSwapRequests(Integer employeeId, boolean allEmployees) {
        if (!allEmployees && employeeId == null) {
            return Result.ok(Collections.<PlanningEmployeeSwapRequest>emptyList());
        }
        return planningEmployeeService.getSwapRequests(employeeId, allEmployees);
    }

    public Result<PlanningEmployeeSwapRequest> requestSwap(
            Integer planningId, Integer employeeId, String reason, boolean emergencyMode) {
        Result<PlanningsEmployee> assignmentResult = getAssignment(planningId, employeeId);
        if (!assignmentResult.isSuccess()) {
            return Result.fail(assignmentResult.getErrors());
        }
        PlanningsEmployee assignment = assignmentResult.getData();
        if (assignment == null) {
            return Result.fail(error("planning.swap.error.assignment"));
        }
        Planning planning = assignment.getPlanning();
        if (planning.getDate() == null || planning.getDate().isBefore(LocalDate.now())) {
            return Result.fail(error("planning.swap.error.past"));
        }
        if (!"SERVICE".equalsIgnoreCase(planning.getType())) {
            return Result.fail(error("planning.swap.error.type"));
        }
        LocalDateTime planningStart = planning.getDate().atTime(
                planning.getStartHour() == null ? LocalTime.MIN : planning.getStartHour());
        if (!LocalDateTime.now().isBefore(planningStart)) {
            return Result.fail(error("planning.swap.error.past"));
        }
        if (!LocalDateTime.now().isBefore(planningStart.minusHours(24)) && !emergencyMode) {
            return Result.fail(error("planning.swap.error.emergency.required"));
        }
        String cleanReason = trim(reason);
        if (cleanReason == null || cleanReason.isEmpty()) {
            return Result.fail(error("planning.swap.error.reason.required"));
        }
        if (cleanReason.length() > 500) {
            return Result.fail(error("planning.swap.error.reason.length"));
        }
        return planningEmployeeService.createSwapRequest(assignment.getId(), employeeId, cleanReason, emergencyMode);
    }

    public Result<List<PlanningSwapProposal>> getProposalsForEmployee(Integer employeeId) {
        if (employeeId == null) return Result.ok(Collections.<PlanningSwapProposal>emptyList());
        planningSwapProposalService.expireOverdue();
        return planningSwapProposalService.getByEmployee(employeeId);
    }

    public Result<List<PlanningSwapProposal>> getProposalsForRequest(Integer requestId) {
        if (requestId == null) return Result.ok(Collections.<PlanningSwapProposal>emptyList());
        return planningSwapProposalService.getByRequest(requestId);
    }

    public Result<Void> proposeReplacements(Integer requestId, List<Integer> replacementEmployeeIds,
                                            Integer reviewerEmployeeId, String reviewComment) {
        if (requestId == null || reviewerEmployeeId == null || replacementEmployeeIds == null
                || replacementEmployeeIds.isEmpty()) {
            return Result.fail(error("planning.swap.error.replacement"));
        }
        Result<PlanningEmployeeSwapRequest> requestResult = planningEmployeeService.getSwapRequest(requestId);
        if (!requestResult.isSuccess() || requestResult.getData() == null) {
            return Result.fail(error("planning.swap.error.notPending"));
        }
        PlanningEmployeeSwapRequest request = requestResult.getData();
        if (request.getStatus() != PlanningSwapStatus.PENDING) {
            return Result.fail(error("planning.swap.error.notPending"));
        }
        List<Integer> cleanIds = new ArrayList<>();
        for (Integer employeeId : replacementEmployeeIds) {
            if (employeeId == null || employeeId.equals(request.getRequestedBy().getId())) continue;
            Result<Void> validation = validateReplacement(request, employeeId);
            if (!validation.isSuccess()) return validation;
            if (!cleanIds.contains(employeeId)) cleanIds.add(employeeId);
        }
        if (cleanIds.isEmpty()) return Result.fail(error("planning.swap.error.replacement"));
        return planningSwapProposalService.propose(requestId, cleanIds, reviewerEmployeeId, trim(reviewComment));
    }

    public Result<Void> acceptProposal(Integer proposalId, Integer employeeId) {
        if (proposalId == null || employeeId == null) {
            return Result.fail(error("planning.swap.proposal.error.unavailable"));
        }
        Result<List<PlanningSwapProposal>> proposalsResult = planningSwapProposalService.getByEmployee(employeeId);
        if (!proposalsResult.isSuccess()) {
            return Result.fail(proposalsResult.getErrors());
        }
        PlanningSwapProposal selectedProposal = null;
        for (PlanningSwapProposal proposal : proposalsResult.getData()) {
            if (proposalId.equals(proposal.getId())) {
                selectedProposal = proposal;
                break;
            }
        }
        if (selectedProposal == null) {
            return Result.fail(error("planning.swap.proposal.error.unavailable"));
        }
        Result<Void> validation = validateReplacement(selectedProposal.getSwapRequest(), employeeId);
        if (!validation.isSuccess()) {
            return validation;
        }
        return planningSwapProposalService.accept(proposalId, employeeId);
    }

    public Result<Void> declineProposal(Integer proposalId, Integer employeeId) {
        return planningSwapProposalService.decline(proposalId, employeeId);
    }

    private Result<Void> validateReplacement(PlanningEmployeeSwapRequest request, Integer employeeId) {
        Planning planning = request.getPlanningEmployee().getPlanning();
        Result<PlanningsEmployee> existingAssignment = planningEmployeeService.getActiveAssignment(
                planning.getId(), employeeId);
        if (!existingAssignment.isSuccess()) return Result.fail(existingAssignment.getErrors());
        if (existingAssignment.getData() != null) return Result.fail(error("planning.swap.error.alreadyAssigned"));
        Result<List<Employee>> conflicts = planningEmployeeService.findConflictingEmployees(
                planning.getDate(), planning.getStartHour(), planning.getEndHour(),
                Collections.singletonList(employeeId), planning.getId());
        if (!conflicts.isSuccess()) return Result.fail(conflicts.getErrors());
        if (!conflicts.getData().isEmpty()) return Result.fail(error("planning.swap.error.conflict"));
        Result<List<String>> absences = absenceBusiness.findPlanningConflicts(
                Collections.singletonList(employeeId), planning.getDate(), planning.getStartHour(), planning.getEndHour());
        if (!absences.isSuccess()) return Result.fail(absences.getErrors());
        if (!absences.getData().isEmpty()) return Result.fail(error("planning.swap.error.absence"));
        return Result.ok();
    }

    public Result<Void> refuse(Integer requestId, Integer reviewerEmployeeId, String reviewComment) {
        if (requestId == null || reviewerEmployeeId == null) {
            return Result.fail(error("planning.swap.error.review"));
        }
        return planningEmployeeService.reviewSwapRequest(requestId, PlanningSwapStatus.REFUSED,
                null, reviewerEmployeeId, trim(reviewComment));
    }

    public Result<Void> cancel(Integer requestId, Integer employeeId) {
        if (requestId == null || employeeId == null) {
            return Result.fail(error("planning.swap.error.cancel"));
        }
        return planningEmployeeService.cancelSwapRequest(requestId, employeeId);
    }

    public BigDecimal calculateHours(PlanningsEmployee assignment) {
        return assignment == null ? BigDecimal.ZERO : assignment.calculateHours();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Map<String, String> error(String messageKey) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", messageKey);
        return errors;
    }
}
