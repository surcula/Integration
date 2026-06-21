package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines employee-to-department assignment database operations.
 */
public interface IEmployeeDepartmentService {

    /**
     * Retrieves the active department relation for an employee.
     *
     * @param id employee id
     * @return active employee department result
     */
    Result<EmployeeDepartment> getActiveEmployeeDepartmentByEmployeeId(Integer id);

    /**
     * Retrieves active employee department relations for a department.
     *
     * @param departmentId department id
     * @return active employee department list result
     */
    Result<List<EmployeeDepartment>> getActiveEmployeeDepartmentsByDepartmentId(Integer departmentId);

    /**
     * Retrieves employee department relations.
     *
     * @return employee department list result
     */
    Result<List<EmployeeDepartment>> getEmployeeList();

    /**
     * Retrieves all employee department relations, active and historical.
     *
     * @return employee department list result
     */
    Result<List<EmployeeDepartment>> getAll();

    /**
     * Retrieves an employee department relation by its identifier.
     *
     * @param id employee department relation id
     * @return employee department result
     */
    Result<EmployeeDepartment> getById(Integer id);

    /**
     * Creates a new employee department assignment.
     *
     * @param assignment assignment to create
     * @return created employee department result
     */
    Result<EmployeeDepartment> assign(EmployeeDepartment assignment);

    /**
     * Deactivates an employee department assignment with an explicit end date.
     *
     * @param id employee department relation id
     * @param endDate assignment end date
     * @return operation result
     */
    Result<Void> deactivate(Integer id, LocalDate endDate);
}
