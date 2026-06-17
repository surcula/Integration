package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;
import be.atc.erpprojetintegration_1.enums.PlanningStatus;

/**
 * Defines planning-related database operations.
 */
public interface IPlanningService {

    /**
     * Retrieves all active planning entries ordered by date and start hour.
     *
     * @return active planning list result
     */
    Result<List<Planning>> getAllActive();

    /**
     * Retrieves active planning entries assigned to an employee.
     *
     * @param employeeId connected employee id
     * @return assigned active planning list result
     */
    Result<List<Planning>> getActiveByEmployee(Integer employeeId);

    /**
     * Retrieves one planning entry by its identifier.
     *
     * @param id planning id
     * @return planning result
     */
    Result<Planning> getById(Integer id);

    /**
     * Retrieves a planning entry only when it is assigned to the employee.
     *
     * @param id planning id
     * @param employeeId connected employee id
     * @return assigned planning result
     */
    Result<Planning> getByIdForEmployee(Integer id, Integer employeeId);

    /**
     * Creates or updates a planning entry.
     *
     * @param planning planning entry to save
     * @return saved planning result
     */
    Result<Planning> save(Planning planning);

    /**
     * Updates planning active status.
     *
     * @param id planning id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);

    /**
     * Updates the status of a planning entry.
     *
     * @param id     planning id
     * @param status new status to apply
     * @return updated planning result
     */
    Result<Planning> setStatus(Integer id, PlanningStatus status);

    /**
     * Retrieves all planning entries assigned to an employee for a given month.
     *
     * @param year       target year
     * @param month      target month (1–12)
     * @param employeeId employee id
     * @return planning list result
     */
    Result<List<Planning>> getByMonthAndEmployee(int year, int month, Integer employeeId);

    /**
     * Retrieves all planning entries belonging to a department for a given month.
     *
     * @param year         target year
     * @param month        target month (1–12)
     * @param departmentId department id
     * @return planning list result
     */
    Result<List<Planning>> getByMonthAndDepartment(int year, int month, Integer departmentId);
}
