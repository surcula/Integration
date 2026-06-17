package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class EmployeeDepartmentBusiness {
    @Inject private IEmployeeDepartmentService employeeDepartmentService;
    @Inject private IEmployeeService employeeService;
    @Inject private IDepartmentService departmentService;

    public Result<List<EmployeeDepartment>> getHistory() {
        return employeeDepartmentService.getAll();
    }

    public Result<List<Employee>> getActiveEmployees() {
        return employeeService.getAllActive();
    }

    public Result<List<Department>> getActiveDepartments() {
        Result<List<Department>> result = departmentService.getAll();
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        List<Department> active = new ArrayList<>();
        for (Department department : result.getData()) {
            if (Boolean.TRUE.equals(department.getIsActive())) active.add(department);
        }
        return Result.ok(active);
    }

    public Result<EmployeeDepartment> assign(Integer employeeId, Integer departmentId, LocalDate startDate) {
        Map<String, String> errors = new HashMap<>();
        if (employeeId == null) errors.put("employee", "L'employe est obligatoire.");
        if (departmentId == null) errors.put("department", "Le departement est obligatoire.");
        if (startDate == null) errors.put("startDate", "La date de debut est obligatoire.");
        if (!errors.isEmpty()) return Result.fail(errors);

        Result<Employee> employeeResult = employeeService.getById(employeeId);
        Result<Department> departmentResult = departmentService.getById(departmentId);
        if (!employeeResult.isSuccess()) return Result.fail(employeeResult.getErrors());
        if (!departmentResult.isSuccess()) return Result.fail(departmentResult.getErrors());
        if (!Boolean.TRUE.equals(employeeResult.getData().getIsActive()) ||
                !Boolean.TRUE.equals(departmentResult.getData().getIsActive())) {
            errors.put("inactive", "L'employe et le departement doivent etre actifs.");
            return Result.fail(errors);
        }

        Result<EmployeeDepartment> current = employeeDepartmentService
                .getActiveEmployeeDepartmentByEmployeeId(employeeId);
        if (!current.isSuccess() && (current.getErrors() == null || !current.getErrors().containsKey("notFound"))) {
            return Result.fail(current.getErrors());
        }
        if (current.isSuccess() && current.getData().getDepartment().getId().equals(departmentId)) {
            errors.put("sameDepartment", "L'employe est deja affecte a ce departement.");
            return Result.fail(errors);
        }
        if (current.isSuccess() && current.getData().getStartDate() != null &&
                !startDate.isAfter(current.getData().getStartDate())) {
            errors.put("startDate", "Le transfert doit commencer apres le debut de l'affectation actuelle.");
            return Result.fail(errors);
        }

        EmployeeDepartment assignment = new EmployeeDepartment();
        assignment.setEmployee(employeeResult.getData());
        assignment.setDepartment(departmentResult.getData());
        assignment.setStartDate(startDate);
        assignment.setIsActive(true);
        return employeeDepartmentService.assign(assignment);
    }

    public Result<Void> deactivate(Integer id, LocalDate endDate) {
        Map<String, String> errors = new HashMap<>();
        if (id == null || endDate == null) {
            errors.put("required", "L'affectation et la date de fin sont obligatoires.");
            return Result.fail(errors);
        }
        Result<EmployeeDepartment> result = employeeDepartmentService.getById(id);
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        if (!Boolean.TRUE.equals(result.getData().getIsActive())) {
            errors.put("inactive", "Cette affectation est deja terminee.");
            return Result.fail(errors);
        }
        if (result.getData().getStartDate() != null && endDate.isBefore(result.getData().getStartDate())) {
            errors.put("endDate", "La date de fin ne peut pas preceder la date de debut.");
            return Result.fail(errors);
        }
        return employeeDepartmentService.deactivate(id, endDate);
    }
}
