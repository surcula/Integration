package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines department head database operations.
 */
public interface IDepartmentHeadService {
    Result<List<DepartmentHead>> getAllActive();
    Result<List<DepartmentHead>> getAll();
    Result<DepartmentHead> getById(Integer id);
    Result<DepartmentHead> getActiveBySuperiorAndDepartment(Integer superiorId, Integer departmentId);
    Result<DepartmentHead> getActiveByDepartmentId(Integer departmentId);
    Result<List<DepartmentHead>> getActiveByEmployeeId(Integer employeeId);
    Result<DepartmentHead> create(DepartmentHead departmentHead);
    Result<DepartmentHead> assign(DepartmentHead departmentHead);
    Result<Void> deactivate(Integer id);
    Result<Void> deactivate(Integer id, LocalDate endDate);
}
