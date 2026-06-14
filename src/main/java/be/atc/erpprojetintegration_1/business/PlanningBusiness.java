package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.interfaces.IPlanningEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.IPlanningService;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.ArrayList;
import be.atc.erpprojetintegration_1.enums.PlanningStatus;

@ApplicationScoped
public class PlanningBusiness {

    @Inject
    private IPlanningService planningService;

    @Inject
    private IPlanningEmployeeService planningEmployeeService;

    @Inject
    private AbsenceBusiness absenceBusiness;

    @Inject
    private IDepartmentHeadService departmentHeadService;

    @Inject
    private IEmployeeDepartmentService employeeDepartmentService;

    public Result<List<Integer>> getManagedDepartmentIds(Integer employeeId) {
        if (employeeId == null) return Result.ok(new ArrayList<Integer>());
        Result<List<DepartmentHead>> result = departmentHeadService.getActiveByEmployeeId(employeeId);
        if (!result.isSuccess()) return Result.fail(result.getErrors());
        return Result.ok(result.getData().stream().map(head -> head.getDepartment().getId()).collect(Collectors.toList()));
    }

    public Result<List<Planning>> getAccessibleActive(Integer actorEmployeeId, boolean globalAccess) {
        Result<List<Planning>> result = planningService.getAllActive();
        if (!result.isSuccess() || globalAccess) return result;
        Result<List<Integer>> managedResult = getManagedDepartmentIds(actorEmployeeId);
        if (!managedResult.isSuccess()) return Result.fail(managedResult.getErrors());
        List<Integer> managedIds = managedResult.getData();
        return Result.ok(result.getData().stream()
                .filter(planning -> planning.getDepartment() != null
                        && managedIds.contains(planning.getDepartment().getId()))
                .collect(Collectors.toList()));
    }

    public Result<Planning> getByIdForManager(Integer planningId, Integer actorEmployeeId, boolean globalAccess) {
        Result<Planning> result = getById(planningId);
        if (!result.isSuccess()) return result;
        Result<Void> access = authorizeDepartment(result.getData().getDepartment(), actorEmployeeId, globalAccess);
        return access.isSuccess() ? result : Result.fail(access.getErrors());
    }

    public Result<List<Planning>> getAllActive() {
        return planningService.getAllActive();
    }

    public Result<List<Planning>> getActiveByEmployee(Integer employeeId) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "planning.error.employee.required");
            return Result.fail(errors);
        }
        return planningService.getActiveByEmployee(employeeId);
    }

    public Result<Planning> getById(Integer planningId) {
        if (planningId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("planningId", "planning.error.id.required");
            return Result.fail(errors);
        }
        return planningService.getById(planningId);
    }

    public Result<Planning> getByIdForEmployee(Integer planningId, Integer employeeId) {
        if (planningId == null || employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("planning", "planning.error.access.denied");
            return Result.fail(errors);
        }
        return planningService.getByIdForEmployee(planningId, employeeId);
    }

    public Result<List<Employee>> getAssignedEmployees(Integer planningId) {
        if (planningId == null) {
            return Result.ok(java.util.Collections.<Employee>emptyList());
        }
        return planningEmployeeService.getActiveEmployees(planningId);
    }

    public Result<Planning> save(Planning planning, List<Integer> employeeIds,
                                 Integer actorEmployeeId, boolean globalAccess) {
        Result<Void> accessResult = authorizePlanning(planning, employeeIds, actorEmployeeId, globalAccess);
        if (!accessResult.isSuccess()) return Result.fail(accessResult.getErrors());
        Result<Void> availabilityResult = validateAvailability(planning, employeeIds);
        if (!availabilityResult.isSuccess()) {
            return Result.fail(availabilityResult.getErrors());
        }

        planning.setNote(trim(planning.getNote()));
        planning.setType(trim(planning.getType()));
        planning.setDescription(trim(planning.getDescription()));
        planning.setIsActive(true);
        if (planning.getStatus() == null) planning.setStatus(PlanningStatus.DRAFT);

        Result<Planning> planningResult = planningService.save(planning);
        if (!planningResult.isSuccess()) {
            return planningResult;
        }

        Result<Void> assignmentResult = planningEmployeeService.replaceAssignments(
                planningResult.getData(), employeeIds);
        if (!assignmentResult.isSuccess()) {
            return Result.fail(assignmentResult.getErrors());
        }

        return planningResult;
    }

    public Result<List<Planning>> saveRecurring(
            Planning template, List<Integer> employeeIds, List<LocalDate> dates,
            Integer actorEmployeeId, boolean globalAccess) {
        if (template == null || template.getId() != null || dates == null || dates.isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("recurrence", "planning.error.recurrence.invalid");
            return Result.fail(errors);
        }

        List<Planning> occurrences = new ArrayList<>();
        for (LocalDate date : dates) {
            Planning occurrence = copyForDate(template, date);
            Result<Void> check = validateAvailability(occurrence, employeeIds);
            if (!check.isSuccess()) {
                Map<String, String> errors = new HashMap<>(check.getErrors());
                errors.put("recurrenceDate", date.toString());
                return Result.fail(errors);
            }
            occurrences.add(occurrence);
        }

        List<Planning> saved = new ArrayList<>();
        for (Planning occurrence : occurrences) {
            Result<Planning> result = save(occurrence, employeeIds, actorEmployeeId, globalAccess);
            if (!result.isSuccess()) {
                return Result.fail(result.getErrors());
            }
            saved.add(result.getData());
        }
        return Result.ok(saved);
    }

    private Result<Void> validateAvailability(Planning planning, List<Integer> employeeIds) {
        Result<Void> validationResult = validate(planning);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        Result<List<Employee>> conflictResult = planningEmployeeService.findConflictingEmployees(
                planning.getDate(),
                planning.getStartHour(),
                planning.getEndHour(),
                employeeIds,
                planning.getId()
        );

        if (!conflictResult.isSuccess()) {
            return Result.fail(conflictResult.getErrors());
        }

        if (!conflictResult.getData().isEmpty()) {
            String employeeNames = conflictResult.getData().stream()
                    .map(employee -> employee.getFirstName() + " " + employee.getLastName())
                    .collect(Collectors.joining(", "));
            Map<String, String> errors = new HashMap<>();
            errors.put("conflicts", employeeNames);
            return Result.fail(errors);
        }

        Result<List<String>> absenceConflicts = absenceBusiness.findPlanningConflicts(
                employeeIds, planning.getDate(), planning.getStartHour(), planning.getEndHour());
        if (!absenceConflicts.isSuccess()) {
            return Result.fail(absenceConflicts.getErrors());
        }
        if (!absenceConflicts.getData().isEmpty()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("conflicts", String.join(", ", absenceConflicts.getData()) + " (absence approuvee)");
            return Result.fail(errors);
        }
        return Result.ok();
    }

    private Planning copyForDate(Planning source, LocalDate date) {
        Planning copy = new Planning();
        copy.setDate(date);
        copy.setStartHour(source.getStartHour());
        copy.setEndHour(source.getEndHour());
        copy.setNote(source.getNote());
        copy.setType(source.getType());
        copy.setDescription(source.getDescription());
        copy.setDepartment(source.getDepartment());
        copy.setIsActive(true);
        copy.setStatus(PlanningStatus.DRAFT);
        return copy;
    }

    public Result<Void> deactivate(Integer planningId, Integer actorEmployeeId, boolean globalAccess) {
        if (planningId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("planningId", "planning.error.id.required");
            return Result.fail(errors);
        }
        Result<Planning> planningResult = getByIdForManager(planningId, actorEmployeeId, globalAccess);
        if (!planningResult.isSuccess()) return Result.fail(planningResult.getErrors());
        return planningService.setActive(planningId, false);
    }

    public Result<Planning> publish(Integer planningId, Integer actorEmployeeId, boolean globalAccess) {
        Result<Planning> planningResult = getByIdForManager(planningId, actorEmployeeId, globalAccess);
        if (!planningResult.isSuccess()) return planningResult;
        Planning planning = planningResult.getData();
        if (planning.getStatus() == PlanningStatus.CANCELLED) {
            Map<String, String> errors = new HashMap<>();
            errors.put("status", "planning.error.cancelled");
            return Result.fail(errors);
        }
        Result<List<Employee>> employeesResult = getAssignedEmployees(planningId);
        if (!employeesResult.isSuccess()) return Result.fail(employeesResult.getErrors());
        List<Integer> employeeIds = employeesResult.getData().stream()
                .map(Employee::getId).collect(Collectors.toList());
        Result<Void> availability = validateAvailability(planning, employeeIds);
        if (!availability.isSuccess()) return Result.fail(availability.getErrors());
        return planningService.setStatus(planningId, PlanningStatus.PUBLISHED);
    }

    public Result<List<Planning>> getByMonthAndEmployeeForManager(int year, int month, Integer employeeId,
                                                                  Integer actorEmployeeId, boolean globalAccess) {
        if (employeeId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("employeeId", "planning.error.employee.required");
            return Result.fail(errors);
        }
        Result<List<Planning>> result = planningService.getByMonthAndEmployee(year, month, employeeId);
        if (!result.isSuccess() || globalAccess) return result;
        Result<List<Integer>> managedResult = getManagedDepartmentIds(actorEmployeeId);
        if (!managedResult.isSuccess()) return Result.fail(managedResult.getErrors());
        return Result.ok(result.getData().stream()
                .filter(planning -> planning.getDepartment() != null
                        && managedResult.getData().contains(planning.getDepartment().getId()))
                .collect(Collectors.toList()));
    }

    public Result<List<Planning>> getByMonthAndDepartmentForManager(int year, int month, Integer departmentId,
                                                                    Integer actorEmployeeId, boolean globalAccess) {
        if (departmentId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("departmentId", "planning.error.department.required");
            return Result.fail(errors);
        }
        Result<Void> access = authorizeDepartmentId(departmentId, actorEmployeeId, globalAccess);
        if (!access.isSuccess()) return Result.fail(access.getErrors());
        return planningService.getByMonthAndDepartment(year, month, departmentId);
    }

    public Result<Planning> cancel(Integer planningId, Integer actorEmployeeId, boolean globalAccess) {
        Result<Planning> planningResult = getByIdForManager(planningId, actorEmployeeId, globalAccess);
        if (!planningResult.isSuccess()) return planningResult;
        return planningService.setStatus(planningId, PlanningStatus.CANCELLED);
    }

    private Result<Void> authorizePlanning(Planning planning, List<Integer> employeeIds,
                                            Integer actorEmployeeId, boolean globalAccess) {
        if (planning == null || planning.getDepartment() == null) return denied();
        Result<Void> departmentAccess = authorizeDepartment(planning.getDepartment(), actorEmployeeId, globalAccess);
        if (!departmentAccess.isSuccess()) return departmentAccess;
        if (globalAccess || employeeIds == null) return Result.ok();
        for (Integer employeeId : employeeIds) {
            Result<EmployeeDepartment> assignment = employeeDepartmentService
                    .getActiveEmployeeDepartmentByEmployeeId(employeeId);
            if (!assignment.isSuccess() || !planning.getDepartment().getId()
                    .equals(assignment.getData().getDepartment().getId())) return denied();
        }
        return Result.ok();
    }

    private Result<Void> authorizeDepartment(be.atc.erpprojetintegration_1.entities.Department department,
                                              Integer actorEmployeeId, boolean globalAccess) {
        return department == null ? denied() : authorizeDepartmentId(department.getId(), actorEmployeeId, globalAccess);
    }

    private Result<Void> authorizeDepartmentId(Integer departmentId, Integer actorEmployeeId, boolean globalAccess) {
        if (globalAccess) return Result.ok();
        Result<List<Integer>> managedResult = getManagedDepartmentIds(actorEmployeeId);
        if (!managedResult.isSuccess()) return Result.fail(managedResult.getErrors());
        return departmentId != null && managedResult.getData().contains(departmentId) ? Result.ok() : denied();
    }

    private Result<Void> denied() {
        Map<String, String> errors = new HashMap<>();
        errors.put("access", "Vous ne pouvez gerer que le planning de votre departement.");
        return Result.fail(errors);
    }

    private Result<Void> validate(Planning planning) {
        Map<String, String> errors = new HashMap<>();

        if (planning == null) {
            errors.put("planning", "planning.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(planning.getNote(), "note", "planning.error.note.required", errors);
        FormValidator.lengthBetween(planning.getNote(), "note", "planning.error.note.length", 1, 255, errors);
        FormValidator.lengthBetween(planning.getType(), "type", "planning.error.type.length", 0, 50, errors);

        if (planning.getDate() == null) {
            errors.put("date", "planning.error.date.required");
        }

        if ((planning.getStartHour() == null) != (planning.getEndHour() == null)) {
            errors.put("hours", "planning.error.hours.bothRequired");
        } else if (planning.getStartHour() != null
                && planning.getEndHour().equals(planning.getStartHour())) {
            errors.put("hours", "planning.error.hours.same");
        }

        return errors.isEmpty() ? Result.ok() : Result.fail(errors);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
