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
     * Retrieves all department head mandates, including inactive history.
     *
     * @return department head list result
     */
    Result<List<DepartmentHead>> getAll();

    /**
     * Retrieves a department head mandate by its identifier.
     *
     * @param id department head id
     * @return department head result
     */
    Result<DepartmentHead> getById(Integer id);

    /**
     * Retrieves the active mandate for one superior in one department.
     *
     * @param superiorId   superior employee id
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
     * Retrieves active department head mandates held by an employee.
     *
     * @param employeeId employee id
     * @return active mandate list result
     */
    Result<List<DepartmentHead>> getActiveByEmployeeId(Integer employeeId);

    /**
     * Creates a department head mandate.
     *
     * @param departmentHead mandate to create
     * @return created mandate result
     */
    Result<DepartmentHead> create(DepartmentHead departmentHead);

    /**
     * Assigns a department head and closes the previous active mandate of the department.
     *
     * @param departmentHead mandate to assign
     * @return assigned mandate result
     */
    Result<DepartmentHead> assign(DepartmentHead departmentHead);

    /**
     * Reactivates a previously inactive department head mandate.
     *
     * @param id department head id
     * @return operation result
     */
    Result<Void> reactivate(Integer id);

    /**
     * Deactivates a department head mandate with an explicit end date.
     *
     * @param id      department head id
     * @param endDate mandate end date
     * @return operation result
     */
    Result<Void> deactivate(Integer id, LocalDate endDate);
}
