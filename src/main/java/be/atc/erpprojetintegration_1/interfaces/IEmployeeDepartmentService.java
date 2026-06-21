package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines employee department assignment database operations.
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
     * Retrieves active employee department assignments for one department.
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
     * Retrieves all employee department assignments, including inactive history.
     *
     * @return employee department assignment list result
     */
    Result<List<EmployeeDepartment>> getAll();

    /**
     * Retrieves an employee department assignment by its identifier.
     *
     * @param id employee department assignment id
     * @return employee department assignment result
     */
    Result<EmployeeDepartment> getById(Integer id);

    /**
     * Assigns an employee to a department and closes the previous active assignment.
     *
     * @param assignment employee department assignment to create
     * @return created assignment result
     */
    Result<EmployeeDepartment> assign(EmployeeDepartment assignment);

    /**
     * Deactivates an employee department assignment with an explicit end date.
     *
     * @param id      employee department assignment id
     * @param endDate assignment end date
     * @return operation result
     */
    Result<Void> deactivate(Integer id, java.time.LocalDate endDate);
}
