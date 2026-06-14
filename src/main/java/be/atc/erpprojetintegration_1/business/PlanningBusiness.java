package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Planning;
import be.atc.erpprojetintegration_1.interfaces.IPlanningEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.IPlanningService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.ArrayList;

@ApplicationScoped
public class PlanningBusiness {

    @Inject
    private IPlanningService planningService;

    @Inject
    private IPlanningEmployeeService planningEmployeeService;

    @Inject
    private AbsenceBusiness absenceBusiness;

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

    public Result<Planning> save(Planning planning, List<Integer> employeeIds) {
        Result<Void> availabilityResult = validateAvailability(planning, employeeIds);
        if (!availabilityResult.isSuccess()) {
            return Result.fail(availabilityResult.getErrors());
        }

        planning.setNote(trim(planning.getNote()));
        planning.setType(trim(planning.getType()));
        planning.setDescription(trim(planning.getDescription()));
        planning.setIsActive(true);

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
            Planning template, List<Integer> employeeIds, List<LocalDate> dates) {
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
            Result<Planning> result = save(occurrence, employeeIds);
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
        return copy;
    }

    public Result<Void> deactivate(Integer planningId) {
        if (planningId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("planningId", "planning.error.id.required");
            return Result.fail(errors);
        }
        return planningService.setActive(planningId, false);
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
