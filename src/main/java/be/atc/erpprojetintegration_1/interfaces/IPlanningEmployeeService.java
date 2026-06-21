package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.entities.PlanningsEmployee;
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

    /**
     * Retrieves all active assignment records for a planning entry, including employee details.
     *
     * @param planningId planning id
     * @return assignment list result
     */
    Result<List<PlanningsEmployee>> getActiveAssignments(Integer planningId);

    /**
     * Updates the note and performed flag of an assignment.
     *
     * @param assignmentId assignment id
     * @param note         optional note
     * @param performed    whether the assignment was performed
     * @return operation result
     */
    Result<Void> updateAssignment(Integer assignmentId, String note, Boolean performed);

    /**
     * Retrieves the active assignment for a specific employee on a planning entry.
     *
     * @param planningId planning id
     * @param employeeId employee id
     * @return assignment result, or empty data if not assigned
     */
    Result<PlanningsEmployee> getActiveAssignment(Integer planningId, Integer employeeId);

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


    /**
     * Finds employees who would not have 11 hours of rest between two service shifts.
     * Split shifts on the same calendar day are allowed.
     *
     * @param date planning date
     * @param startHour planned start hour
     * @param endHour planned end hour
     * @param employeeIds selected employee ids
     * @param excludedPlanningId planning id excluded during edition
     * @return employee list result containing insufficient-rest conflicts
     */
    Result<List<Employee>> findInsufficientRestEmployees(
            LocalDate date,
            LocalTime startHour,
            LocalTime endHour,
            List<Integer> employeeIds,
            Integer excludedPlanningId
    );

    /**
     * Finds employees who would exceed seven consecutive service days.
     * Multiple service shifts on the same day count as one worked day.
     *
     * @param date planning date
     * @param employeeIds selected employee ids
     * @param excludedPlanningId planning id excluded during edition
     * @return employee list result containing consecutive-service conflicts
     */
    Result<List<Employee>> findExcessiveConsecutiveServiceEmployees(
            LocalDate date,
            List<Integer> employeeIds,
            Integer excludedPlanningId
    );
}
