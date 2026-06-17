package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.interfaces.IPlanningEmployeeService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PlanningEmployeeBusiness {

    @Inject
    private IPlanningEmployeeService planningEmployeeService;

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
            return Result.fail(error("planning.assignment.error.notFound"));
        }
        return planningEmployeeService.getActiveAssignment(planningId, employeeId);
    }

    public BigDecimal calculateHours(PlanningsEmployee assignment) {
        return assignment == null ? BigDecimal.ZERO : assignment.calculateHours();
    }

    private Map<String, String> error(String messageKey) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", messageKey);
        return errors;
    }
}
