package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IEmployeeDepartmentService {

    /**
     * Retrieves the active department relation for an employee.
     *
     * @param id employee id
     * @return active employee department result
     */
    Result<EmployeeDepartment> getActiveEmployeeDepartmentByEmployeeId(Integer id);

    /**
     * Retrieves employee department relations.
     *
     * @return employee department list result
     */
    Result<List<EmployeeDepartment>> getEmployeeList();
}
