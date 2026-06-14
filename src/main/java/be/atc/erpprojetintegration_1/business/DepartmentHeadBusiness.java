package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
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
public class DepartmentHeadBusiness {
    @Inject private IDepartmentHeadService departmentHeadService;
    @Inject private IDepartmentService departmentService;
    @Inject private IEmployeeService employeeService;
    @Inject private IEmployeeDepartmentService employeeDepartmentService;

    public Result<List<DepartmentHead>> getAll() { return departmentHeadService.getAll(); }

    public Result<List<Department>> getActiveDepartments() {
        Result<List<Department>> result = departmentService.getAll();
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        List<Department> active = new ArrayList<>();
        for (Department department : result.getData()) {
            if (Boolean.TRUE.equals(department.getIsActive())) active.add(department);
        }
        return Result.ok(active);
    }

    public Result<List<Employee>> getEligibleEmployees(Integer departmentId) {
        if (departmentId == null) return Result.ok(new ArrayList<Employee>());
        Result<List<EmployeeDepartment>> result = employeeDepartmentService.getEmployeeList();
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        List<Employee> employees = new ArrayList<>();
        for (EmployeeDepartment assignment : result.getData()) {
            if (departmentId.equals(assignment.getDepartment().getId())) employees.add(assignment.getEmployee());
        }
        return Result.ok(employees);
    }

    public Result<DepartmentHead> assign(Integer departmentId, Integer employeeId, LocalDate startDate) {
        Map<String, String> errors = new HashMap<>();
        if (departmentId == null) errors.put("department", "Le departement est obligatoire.");
        if (employeeId == null) errors.put("employee", "Le chef est obligatoire.");
        if (startDate == null) errors.put("startDate", "La date de debut est obligatoire.");
        if (!errors.isEmpty()) return Result.fail(errors);

        Result<Department> departmentResult = departmentService.getById(departmentId);
        Result<Employee> employeeResult = employeeService.getById(employeeId);
        if (!departmentResult.isSuccess()) return Result.fail(departmentResult.getErrors());
        if (!employeeResult.isSuccess()) return Result.fail(employeeResult.getErrors());
        if (!Boolean.TRUE.equals(departmentResult.getData().getIsActive())
                || !Boolean.TRUE.equals(employeeResult.getData().getIsActive())) {
            errors.put("inactive", "Le departement et l'employe doivent etre actifs.");
            return Result.fail(errors);
        }

        Result<EmployeeDepartment> assignmentResult = employeeDepartmentService
                .getActiveEmployeeDepartmentByEmployeeId(employeeId);
        if (!assignmentResult.isSuccess()
                || !departmentId.equals(assignmentResult.getData().getDepartment().getId())) {
            errors.put("assignment", "Le chef doit etre actuellement affecte a ce departement.");
            return Result.fail(errors);
        }

        Result<DepartmentHead> currentResult = departmentHeadService.getActiveByDepartmentId(departmentId);
        if (currentResult.isSuccess()) {
            DepartmentHead current = currentResult.getData();
            if (employeeId.equals(current.getSuperior().getId())) {
                errors.put("sameHead", "Cet employe est deja chef de ce departement.");
                return Result.fail(errors);
            }
            if (current.getStartDate() != null && !startDate.isAfter(current.getStartDate())) {
                errors.put("startDate", "Le nouveau mandat doit commencer apres le mandat actuel.");
                return Result.fail(errors);
            }
        } else if (currentResult.getErrors() == null || !currentResult.getErrors().containsKey("notFound")) {
            return Result.fail(currentResult.getErrors());
        }

        DepartmentHead departmentHead = new DepartmentHead();
        departmentHead.setDepartment(departmentResult.getData());
        departmentHead.setSuperior(employeeResult.getData());
        departmentHead.setStartDate(startDate);
        departmentHead.setIsActive(true);
        return departmentHeadService.assign(departmentHead);
    }

    public Result<Void> deactivate(Integer id, LocalDate endDate) {
        Map<String, String> errors = new HashMap<>();
        if (id == null || endDate == null) {
            errors.put("required", "Le mandat et la date de fin sont obligatoires.");
            return Result.fail(errors);
        }
        Result<DepartmentHead> result = departmentHeadService.getById(id);
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        DepartmentHead head = result.getData();
        if (!Boolean.TRUE.equals(head.getIsActive())) {
            errors.put("inactive", "Ce mandat est deja termine.");
            return Result.fail(errors);
        }
        if (head.getStartDate() != null && endDate.isBefore(head.getStartDate())) {
            errors.put("endDate", "La date de fin ne peut pas preceder la date de debut.");
            return Result.fail(errors);
        }
        return departmentHeadService.deactivate(id, endDate);
    }
}
