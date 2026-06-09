package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines department-related database operations.
 */
public interface IDepartmentService {

    /**
     * Retrieves all departments.
     *
     * @return department list result
     */
    Result<List<Department>> getAll();

    /**
     * Retrieves a department by id.
     *
     * @param id department id
     * @return department result
     */
    Result<Department> getById(Integer id);

    /**
     * Creates a department.
     *
     * @param department department to create
     * @return created department result
     */
    Result<Department> create(Department department);

    /**
     * Updates a department.
     *
     * @param department department to update
     * @return updated department result
     */
    Result<Department> update(Department department);

    /**
     * Updates department active status.
     *
     * @param id department id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
