package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Superior;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;
import java.util.Map;

/**
 * Defines employee superior database operations.
 */
public interface ISuperiorService {

    /**
     * Retrieves all employee superior assignments.
     *
     * @return superior assignment list result
     */
    Result<List<Superior>> getAll();

    Result<List<Superior>> getActiveBySuperiorId(Integer superiorId);

    /**
     * Retrieves the active superior assignment of an employee.
     *
     * @param employeeId supervised employee id
     * @return active superior assignment result
     */
    Result<Superior> getActiveByEmployeeId(Integer employeeId);

    /**
     * Counts active supervised employees grouped by superior and department.
     *
     * @return map where key is superiorId:departmentId and value is managed employee count
     */
    Result<Map<String, Integer>> getManagedEmployeeCountsBySuperiorAndDepartment();

    /**
     * Deactivates active team assignments for a superior in a department.
     *
     * @param superiorEmployeeId superior employee id
     * @param departmentId department id
     * @return operation result
     */
    Result<Void> deactivateTeam(Integer superiorEmployeeId, Integer departmentId);

    /**
     * Retrieves a superior assignment by id.
     *
     * @param id superior assignment id
     * @return superior assignment result
     */
    Result<Superior> getById(Integer id);

    /**
     * Creates a superior assignment.
     *
     * @param superior superior assignment to create
     * @return created superior assignment result
     */
    Result<Superior> create(Superior superior);

    /**
     * Updates a superior assignment.
     *
     * @param superior superior assignment to update
     * @return updated superior assignment result
     */
    Result<Superior> update(Superior superior);

    /**
     * Updates superior assignment active status.
     *
     * @param id superior assignment id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
