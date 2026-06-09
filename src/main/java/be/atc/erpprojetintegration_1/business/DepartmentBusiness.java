package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DepartmentBusiness {

    @Inject
    private IDepartmentService departmentService;

    /**
     * Retrieves all departments for the administration list.
     *
     * @return department list result
     */
    public Result<List<Department>> getAllDepartments() {
        return departmentService.getAll();
    }

    /**
     * Retrieves a department by id.
     *
     * @param id department id
     * @return department result
     */
    public Result<Department> getDepartmentById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "departments.error.id.required");
            return Result.fail(errors);
        }

        return departmentService.getById(id);
    }

    /**
     * Creates or updates a department after validation.
     *
     * @param department department to save
     * @return saved department result
     */
    public Result<Department> saveDepartment(Department department) {
        Result<Void> validationResult = validateDepartment(department);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        trimDepartmentFields(department);

        if (department.getId() == null) {
            department.setIsActive(true);
            return departmentService.create(department);
        }

        return departmentService.update(department);
    }

    /**
     * Updates department active status.
     *
     * @param id department id
     * @param active active status
     * @return operation result
     */
    public Result<Void> setActive(Integer id, boolean active) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "departments.error.id.required");
            return Result.fail(errors);
        }

        return departmentService.setActive(id, active);
    }

    private Result<Void> validateDepartment(Department department) {
        Map<String, String> errors = new HashMap<>();

        if (department == null) {
            errors.put("department", "departments.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(department.getDepartmentName(), "departmentName", "departments.error.name.required", errors);
        FormValidator.lengthBetween(department.getDepartmentName(), "departmentName", "departments.error.name.length", 1, 150, errors);
        FormValidator.lengthBetween(department.getPhone(), "phone", "departments.error.phone.length", 0, 20, errors);
        FormValidator.lengthBetween(department.getEmail(), "email", "departments.error.email.length", 0, 150, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void trimDepartmentFields(Department department) {
        department.setDepartmentName(trim(department.getDepartmentName()));
        department.setPhone(trim(department.getPhone()));
        department.setEmail(trim(department.getEmail()));
        department.setDescription(trim(department.getDescription()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
