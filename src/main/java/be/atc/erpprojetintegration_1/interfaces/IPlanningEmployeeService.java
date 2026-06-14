package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PlanningEmployeeSwapRequest;
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Defines employee assignment operations for planning entries.
 */
public interface IPlanningEmployeeService {

    /**
     * Retrieves employees actively assigned to a planning entry.
     *
     * @param planningId planning id
     * @return assigned employee list result
     */
    Result<List<Employee>> getActiveEmployees(Integer planningId);

    Result<PlanningsEmployee> getActiveAssignment(Integer planningId, Integer employeeId);

    Result<List<PlanningEmployeeSwapRequest>> getSwapRequests(Integer employeeId, boolean allEmployees);

    Result<PlanningEmployeeSwapRequest> getSwapRequest(Integer requestId);

    Result<PlanningEmployeeSwapRequest> createSwapRequest(
            Integer planningEmployeeId, Integer requestedByEmployeeId, String reason);

    Result<Void> reviewSwapRequest(Integer requestId, PlanningSwapStatus status,
                                   Integer replacementEmployeeId, Integer reviewedByEmployeeId,
                                   String reviewComment);

    Result<Void> cancelSwapRequest(Integer requestId, Integer requestedByEmployeeId);

    /**
     * Replaces all active employee assignments for a planning entry.
     *
     * @param planning planning entry
     * @param employeeIds employee ids to assign
     * @return operation result
     */
    Result<Void> replaceAssignments(Planning planning, List<Integer> employeeIds);

    /**
     * Finds selected employees assigned to an incompatible planning entry on the same date.
     *
     * @param date planning date
     * @param startHour start hour, or null for an all-day entry
     * @param endHour end hour, or null for an all-day entry
     * @param employeeIds selected employee ids
     * @param excludedPlanningId planning id excluded during edition
     * @return conflicting employee list result
     */
    Result<List<Employee>> findConflictingEmployees(
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            List<Integer> employeeIds,
            Integer excludedPlanningId
    );
}
