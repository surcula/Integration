package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines department head database operations.
 */
public interface IDepartmentHeadService {

    /**
     * Retrieves all active department head mandates.
     *
     * @return active department head list result
     */
    Result<List<DepartmentHead>> getAllActive();

    /**
     * Retrieves all department head mandates, active and historical.
     *
     * @return department head list result
     */
    Result<List<DepartmentHead>> getAll();

    /**
     * Retrieves a department head mandate by its identifier.
     *
     * @param id department head mandate id
     * @return department head result
     */
    Result<DepartmentHead> getById(Integer id);

    /**
     * Retrieves the active mandate for a specific employee and department.
     *
     * @param superiorId employee acting as department head
     * @param departmentId department id
     * @return active department head result
     */
    Result<DepartmentHead> getActiveBySuperiorAndDepartment(Integer superiorId, Integer departmentId);

    /**
     * Retrieves the active head mandate of a department.
     *
     * @param departmentId department id
     * @return active department head result
     */
    Result<DepartmentHead> getActiveByDepartmentId(Integer departmentId);

    /**
     * Retrieves all active department head mandates held by an employee.
     *
     * @param employeeId employee id
     * @return active department head list result
     */
    Result<List<DepartmentHead>> getActiveByEmployeeId(Integer employeeId);

    /**
     * Creates a department head mandate.
     *
     * @param departmentHead mandate to create
     * @return created department head result
     */
    Result<DepartmentHead> create(DepartmentHead departmentHead);

    /**
     * Assigns a new head to a department and closes the previous active mandate if needed.
     *
     * @param departmentHead mandate to assign
     * @return assigned department head result
     */
    Result<DepartmentHead> assign(DepartmentHead departmentHead);

    /**
     * Updates the active status of a department head mandate.
     *
     * @param id department head mandate id
     * @param active active status to apply
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);

    /**
     * Deactivates a department head mandate using the current date.
     *
     * @param id department head mandate id
     * @return operation result
     */
    Result<Void> deactivate(Integer id);

    /**
     * Deactivates a department head mandate with an explicit end date.
     *
     * @param id department head mandate id
     * @param endDate mandate end date
     * @return operation result
     */
    Result<Void> deactivate(Integer id, LocalDate endDate);
}
