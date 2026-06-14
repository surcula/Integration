package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.tools.Result;

import java.time.LocalDate;
import java.util.List;

public interface IDepartmentHeadService {
    Result<List<DepartmentHead>> getAll();
    Result<DepartmentHead> getById(Integer id);
    Result<DepartmentHead> getActiveByDepartmentId(Integer departmentId);
    Result<List<DepartmentHead>> getActiveByEmployeeId(Integer employeeId);
    Result<DepartmentHead> assign(DepartmentHead departmentHead);
    Result<Void> deactivate(Integer id, LocalDate endDate);
}
