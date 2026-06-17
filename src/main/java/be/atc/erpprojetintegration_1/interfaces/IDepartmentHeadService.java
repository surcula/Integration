package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines department head database operations.
 */
public interface IDepartmentHeadService {

    /**
     * Retrieves all active department heads.
     *
     * @return department head list result
     */
    Result<List<DepartmentHead>> getAllActive();

    /**
     * Retrieves a department head by id.
     *
     * @param id department head id
     * @return department head result
     */
    Result<DepartmentHead> getById(Integer id);

    /**
     * Retrieves an active department head for a superior and a department.
     *
     * @param superiorId superior employee id
     * @param departmentId department id
     * @return department head result
     */
    Result<DepartmentHead> getActiveBySuperiorAndDepartment(Integer superiorId, Integer departmentId);

    /**
     * Creates a department head.
     *
     * @param departmentHead department head to create
     * @return created department head result
     */
    Result<DepartmentHead> create(DepartmentHead departmentHead);

    /**
     * Deactivates a department head.
     *
     * @param id department head id
     * @return operation result
     */
    Result<Void> deactivate(Integer id);
}
