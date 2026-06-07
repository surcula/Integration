package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.dto.EmployeeProfileDto;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IEmployeeDepartmentService {

    Result<EmployeeDepartment> getActiveEmployeeDepartmentByEmployeeId(Integer id);

    Result<List<EmployeeDepartment>> getEmployeeList();
}
