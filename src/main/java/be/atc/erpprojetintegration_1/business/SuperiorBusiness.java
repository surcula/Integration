package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.dto.SuperiorEditDto;
import be.atc.erpprojetintegration_1.dto.SuperiorListDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.entities.Superior;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.interfaces.ISuperiorService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class SuperiorBusiness {

    @Inject
    private ISuperiorService superiorService;

    @Inject
    private IEmployeeService employeeService;

    @Inject
    private IDepartmentHeadService departmentHeadService;

    @Inject
    private IDepartmentService departmentService;

    @Inject
    private IEmployeeDepartmentService employeeDepartmentService;

    /**
     * Retrieves all superior assignments for the administration list.
     *
     * @return superior assignment list result
     */
    public Result<List<SuperiorListDto>> getAllSuperiors() {
        Result<List<DepartmentHead>> departmentHeadsResult = departmentHeadService.getAll();

        if (!departmentHeadsResult.isSuccess()) {
            return Result.fail(departmentHeadsResult.getErrors());
        }

        Result<Map<String, Integer>> countResult = superiorService.getManagedEmployeeCountsBySuperiorAndDepartment();

        if (!countResult.isSuccess()) {
            return Result.fail(countResult.getErrors());
        }

        List<SuperiorListDto> superiors = buildSuperiorList(departmentHeadsResult.getData(), countResult.getData());

        return Result.ok(superiors);
    }

    /**
     * Retrieves active employees assigned to a superior.
     *
     * @param superiorEmployeeId superior employee id
     * @return assignment list result
     */
    public Result<List<SuperiorListDto>> getEmployeesBySuperior(Integer superiorEmployeeId) {
        return getEmployeesBySuperior(superiorEmployeeId, null);
    }

    /**
     * Retrieves active employees assigned to a superior in a selected department.
     *
     * @param superiorEmployeeId superior employee id
     * @param departmentId selected department id
     * @return assignment list result
     */
    public Result<List<SuperiorListDto>> getEmployeesBySuperior(Integer superiorEmployeeId, Integer departmentId) {
        if (superiorEmployeeId == null) {
            return Result.ok(new ArrayList<>());
        }

        Result<List<Superior>> result = superiorService.getActiveBySuperiorId(superiorEmployeeId);

        if (!result.isSuccess()) {
            return Result.fail(result.getErrors());
        }

        Result<List<EmployeeDepartment>> employeeDepartmentsResult = departmentId == null
                ? employeeDepartmentService.getEmployeeList()
                : employeeDepartmentService.getActiveEmployeeDepartmentsByDepartmentId(departmentId);
        if (!employeeDepartmentsResult.isSuccess()) {
            return Result.fail(employeeDepartmentsResult.getErrors());
        }

        List<SuperiorListDto> employees = result.getData()
                .stream()
                .filter(superior -> Boolean.TRUE.equals(superior.getIsActive()))
                .filter(superior -> superior.getSuperior() != null)
                .filter(superior -> superiorEmployeeId.equals(superior.getSuperior().getId()))
                .filter(superior -> departmentId == null || departmentId.equals(getActiveDepartmentId(superior.getEmployee(), employeeDepartmentsResult.getData())))
                .map(superior -> toListDto(superior, employeeDepartmentsResult.getData()))
                .collect(Collectors.toList());

        return Result.ok(employees);
    }

    /**
     * Retrieves active employees for superior assignment forms.
     *
     * @return active employee list result
     */
    public Result<List<Employee>> getActiveEmployees() {
        return employeeService.getAllActiveWithDepartments();
    }

    /**
     * Retrieves employees available for assignment to the selected superior.
     *
     * @param superiorEmployeeId selected superior employee id
     * @return available employee list result
     */
    public Result<List<Employee>> getAvailableEmployeesForSuperior(Integer superiorEmployeeId) {
        return getAvailableEmployeesForSuperior(superiorEmployeeId, null);
    }

    /**
     * Retrieves employees available for assignment to the selected superior and department.
     *
     * @param superiorEmployeeId selected superior employee id
     * @param selectedDepartmentId selected department id
     * @return available employee list result
     */
    public Result<List<Employee>> getAvailableEmployeesForSuperior(Integer superiorEmployeeId, Integer selectedDepartmentId) {
        Result<List<Employee>> employeesResult = employeeService.getAllActiveWithDepartments();

        if (!employeesResult.isSuccess()) {
            return Result.fail(employeesResult.getErrors());
        }

        Result<List<EmployeeDepartment>> employeeDepartmentsResult = selectedDepartmentId == null
                ? employeeDepartmentService.getEmployeeList()
                : employeeDepartmentService.getActiveEmployeeDepartmentsByDepartmentId(selectedDepartmentId);
        if (!employeeDepartmentsResult.isSuccess()) {
            return Result.fail(employeeDepartmentsResult.getErrors());
        }

        if (superiorEmployeeId == null) {
            return Result.ok(new ArrayList<>());
        }

        Employee superiorEmployee = findEmployee(employeesResult.getData(), superiorEmployeeId);
        Integer departmentId = selectedDepartmentId != null
                ? selectedDepartmentId
                : getActiveDepartmentId(superiorEmployee, employeeDepartmentsResult.getData());

        if (departmentId == null) {
            return Result.ok(new ArrayList<>());
        }

        Result<List<SuperiorListDto>> assignedResult = getEmployeesBySuperior(superiorEmployeeId, departmentId);
        if (!assignedResult.isSuccess()) {
            return Result.fail(assignedResult.getErrors());
        }

        List<Integer> assignedEmployeeIds = assignedResult.getData()
                .stream()
                .map(SuperiorListDto::getId)
                .collect(Collectors.toList());

        List<Employee> availableEmployees = employeesResult.getData()
                .stream()
                .filter(employee -> !superiorEmployeeId.equals(employee.getId()))
                .filter(employee -> departmentId.equals(getActiveDepartmentId(employee, employeeDepartmentsResult.getData())))
                .filter(employee -> !assignedEmployeeIds.contains(employee.getId()))
                .collect(Collectors.toList());

        return Result.ok(availableEmployees);
    }

    /**
     * Retrieves active departments for superior forms.
     *
     * @return active department list result
     */
    public Result<List<Department>> getActiveDepartments() {
        Result<List<Department>> departmentsResult = departmentService.getAll();

        if (!departmentsResult.isSuccess()) {
            return Result.fail(departmentsResult.getErrors());
        }

        List<Department> departments = departmentsResult.getData()
                .stream()
                .filter(department -> Boolean.TRUE.equals(department.getIsActive()))
                .collect(Collectors.toList());

        return Result.ok(departments);
    }

    /**
     * Creates the department head row if it does not already exist.
     *
     * @param superiorEmployeeId superior employee id
     * @param departmentId department id
     * @return operation result
     */
    public Result<Void> createDepartmentHeadIfNeeded(Integer superiorEmployeeId, Integer departmentId) {
        Map<String, String> errors = new HashMap<>();

        if (superiorEmployeeId == null) {
            errors.put("superiorId", "superiors.error.superior.required");
        }

        if (departmentId == null) {
            errors.put("departmentId", "superiors.error.department.required");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        Result<List<Employee>> employeesResult = employeeService.getAllActiveWithDepartments();

        if (!employeesResult.isSuccess()) {
            return Result.fail(employeesResult.getErrors());
        }

        Employee superiorEmployee = findEmployee(employeesResult.getData(), superiorEmployeeId);

        if (superiorEmployee == null || !departmentId.equals(getActiveDepartmentId(superiorEmployee))) {
            errors.put("departmentId", "superiors.error.superior.department.required");
            return Result.fail(errors);
        }

        Result<DepartmentHead> existingResult = departmentHeadService.getActiveBySuperiorAndDepartment(superiorEmployeeId, departmentId);

        if (existingResult.isSuccess()) {
            return Result.ok();
        }

        Result<List<DepartmentHead>> departmentHeadsResult = departmentHeadService.getAll();
        if (!departmentHeadsResult.isSuccess()) {
            return Result.fail(departmentHeadsResult.getErrors());
        }

        for (DepartmentHead existingDepartmentHead : departmentHeadsResult.getData()) {
            if (isSameDepartmentHead(existingDepartmentHead, superiorEmployeeId, departmentId)) {
                return departmentHeadService.reactivate(existingDepartmentHead.getId());
            }
        }

        Result<Department> departmentResult = departmentService.getById(departmentId);
        if (!departmentResult.isSuccess()) {
            return Result.fail(departmentResult.getErrors());
        }

        DepartmentHead departmentHead = new DepartmentHead();
        departmentHead.setSuperior(superiorEmployee);
        departmentHead.setDepartment(departmentResult.getData());
        departmentHead.setStartDate(LocalDate.now());
        departmentHead.setIsActive(true);

        Result<DepartmentHead> createdResult = departmentHeadService.create(departmentHead);

        if (!createdResult.isSuccess()) {
            return Result.fail(createdResult.getErrors());
        }

        return Result.ok();
    }

    /**
     * Retrieves a superior assignment and maps it for the edit form.
     *
     * @param id superior assignment id
     * @return superior edit dto result
     */
    public Result<SuperiorEditDto> getSuperiorForEdit(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "superiors.error.id.required");
            return Result.fail(errors);
        }

        Result<Superior> result = superiorService.getById(id);

        if (!result.isSuccess()) {
            return Result.fail(result.getErrors());
        }

        return Result.ok(toDto(result.getData()));
    }

    /**
     * Creates or updates a superior assignment after validation.
     *
     * @param dto superior edit dto
     * @return saved superior edit dto result
     */
    public Result<SuperiorEditDto> saveSuperior(SuperiorEditDto dto) {
        Result<Void> validationResult = validateSuperior(dto);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Result<Employee> employeeResult = employeeService.getById(dto.getEmployeeId());
        if (!employeeResult.isSuccess()) {
            return Result.fail(employeeResult.getErrors());
        }

        Result<Employee> superiorEmployeeResult = employeeService.getById(dto.getSuperiorId());
        if (!superiorEmployeeResult.isSuccess()) {
            return Result.fail(superiorEmployeeResult.getErrors());
        }

        Superior superior;

        if (dto.getId() == null) {
            superior = new Superior();
            superior.setIsActive(true);
        } else {
            Result<Superior> existingResult = superiorService.getById(dto.getId());

            if (!existingResult.isSuccess()) {
                return Result.fail(existingResult.getErrors());
            }

            superior = existingResult.getData();
        }

        updateSuperiorFromDto(dto, superior, employeeResult.getData(), superiorEmployeeResult.getData());

        Result<Superior> savedResult = dto.getId() == null
                ? superiorService.create(superior)
                : superiorService.update(superior);

        if (!savedResult.isSuccess()) {
            return Result.fail(savedResult.getErrors());
        }

        return Result.ok(toDto(savedResult.getData()));
    }

    /**
     * Adds an active employee to a superior team.
     *
     * @param superiorEmployeeId superior employee id
     * @param employeeId employee id
     * @return operation result
     */
    public Result<Void> addEmployeeToSuperior(Integer superiorEmployeeId, Integer employeeId) {
        SuperiorEditDto dto = new SuperiorEditDto();
        dto.setSuperiorId(superiorEmployeeId);
        dto.setEmployeeId(employeeId);
        dto.setStartDate(LocalDate.now().toString());
        dto.setIsActive(true);

        Result<SuperiorEditDto> result = saveSuperior(dto);

        if (!result.isSuccess()) {
            return Result.fail(result.getErrors());
        }

        return Result.ok();
    }

    /**
     * Updates a superior assignment active status.
     *
     * @param id superior assignment id
     * @param active active status
     * @return operation result
     */
    public Result<Void> setActive(Integer id, boolean active) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "superiors.error.id.required");
            return Result.fail(errors);
        }

        return superiorService.setActive(id, active);
    }

    /**
     * Removes an employee from a superior team using a soft delete.
     *
     * @param relationId superior assignment id
     * @return operation result
     */
    public Result<Void> removeEmployeeFromSuperior(Integer relationId) {
        return setActive(relationId, false);
    }

    /**
     * Removes all active employees from a superior team without removing the department head.
     *
     * @param superiorEmployeeId superior employee id
     * @param departmentId department id
     * @return operation result
     */
    public Result<Void> removeTeam(Integer superiorEmployeeId, Integer departmentId) {
        Map<String, String> errors = new HashMap<>();

        if (superiorEmployeeId == null) {
            errors.put("superiorId", "superiors.error.superior.required");
        }

        if (departmentId == null) {
            errors.put("departmentId", "superiors.error.department.required");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return superiorService.deactivateTeam(superiorEmployeeId, departmentId);
    }

    /**
     * Removes a department head and deactivates supervised employees in that department.
     *
     * @param departmentHeadId department head id
     * @param endDate mandate end date
     * @return operation result
     */
    public Result<Void> removeDepartmentHead(Integer departmentHeadId, LocalDate endDate) {
        Map<String, String> errors = new HashMap<>();
        if (departmentHeadId == null) {
            errors.put("id", "superiors.departmentHead.id.required");
        }
        if (endDate == null) {
            errors.put("endDate", "superiors.departmentHead.endDate.required");
        }
        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        Result<DepartmentHead> departmentHeadResult = departmentHeadService.getById(departmentHeadId);

        if (!departmentHeadResult.isSuccess()) {
            return Result.fail(departmentHeadResult.getErrors());
        }

        DepartmentHead departmentHead = departmentHeadResult.getData();
        Result<List<Superior>> assignmentsResult = superiorService.getAll();

        if (!assignmentsResult.isSuccess()) {
            return Result.fail(assignmentsResult.getErrors());
        }

        for (Superior assignment : assignmentsResult.getData()) {
            if (isActiveAssignmentForDepartmentHead(departmentHead, assignment)) {
                Result<Void> removeResult = superiorService.setActive(assignment.getId(), false);

                if (!removeResult.isSuccess()) {
                    return Result.fail(removeResult.getErrors());
                }
            }
        }

        return departmentHeadService.deactivate(departmentHeadId, endDate);
    }

    public Result<Void> reactivateDepartmentHead(Integer departmentHeadId) {
        if (departmentHeadId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "superiors.departmentHead.id.required");
            return Result.fail(errors);
        }

        return departmentHeadService.reactivate(departmentHeadId);
    }

    private Result<Void> validateSuperior(SuperiorEditDto dto) {
        Map<String, String> errors = new HashMap<>();

        if (dto == null) {
            errors.put("superior", "superiors.error.form.invalid");
            return Result.fail(errors);
        }

        if (dto.getEmployeeId() == null) {
            errors.put("employeeId", "superiors.error.employee.required");
        }

        if (dto.getSuperiorId() == null) {
            errors.put("superiorId", "superiors.error.superior.required");
        }

        if (dto.getEmployeeId() != null && dto.getEmployeeId().equals(dto.getSuperiorId())) {
            errors.put("superiorId", "superiors.error.sameEmployee");
        }

        validateDate(dto.getStartDate(), "startDate", "superiors.error.startDate.invalid", errors);
        validateDate(dto.getEndDate(), "endDate", "superiors.error.endDate.invalid", errors);

        validateSameDepartment(dto, errors);
        validateNoActiveDuplicate(dto, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void validateNoActiveDuplicate(SuperiorEditDto dto, Map<String, String> errors) {
        if (dto.getEmployeeId() == null || dto.getSuperiorId() == null) {
            return;
        }

        Result<List<Superior>> result = superiorService.getAll();

        if (!result.isSuccess()) {
            errors.put("duplicate", "superiors.error.load");
            return;
        }

        for (Superior superior : result.getData()) {
            if (Boolean.TRUE.equals(superior.getIsActive())
                    && superior.getEmployee() != null
                    && superior.getSuperior() != null
                    && dto.getEmployeeId().equals(superior.getEmployee().getId())
                    && dto.getSuperiorId().equals(superior.getSuperior().getId())
                    && (dto.getId() == null || !dto.getId().equals(superior.getId()))) {
                errors.put("duplicate", "superiors.error.duplicate");
                return;
            }
        }
    }

    private void validateSameDepartment(SuperiorEditDto dto, Map<String, String> errors) {
        if (dto.getEmployeeId() == null || dto.getSuperiorId() == null) {
            return;
        }

        Result<List<Employee>> employeesResult = employeeService.getAllActiveWithDepartments();

        if (!employeesResult.isSuccess()) {
            errors.put("department", "superiors.error.department.load");
            return;
        }

        Integer employeeDepartmentId = getActiveDepartmentId(findEmployee(employeesResult.getData(), dto.getEmployeeId()));
        Integer superiorDepartmentId = getActiveDepartmentId(findEmployee(employeesResult.getData(), dto.getSuperiorId()));

        if (employeeDepartmentId == null || superiorDepartmentId == null || !employeeDepartmentId.equals(superiorDepartmentId)) {
            errors.put("superiorId", "superiors.error.sameDepartment");
        }
    }

    private Employee findEmployee(List<Employee> employees, Integer employeeId) {
        if (employees == null || employeeId == null) {
            return null;
        }

        for (Employee employee : employees) {
            if (employeeId.equals(employee.getId())) {
                return employee;
            }
        }

        return null;
    }

    private Integer getActiveDepartmentId(Employee employee) {
        return getActiveDepartmentId(employee, null);
    }

    private Integer getActiveDepartmentId(Employee employee, List<EmployeeDepartment> employeeDepartments) {
        if (employee != null && employeeDepartments != null) {
            for (EmployeeDepartment employeeDepartment : employeeDepartments) {
                if (employeeDepartment.getEmployee() != null
                        && employee.getId() != null
                        && employee.getId().equals(employeeDepartment.getEmployee().getId())
                        && Boolean.TRUE.equals(employeeDepartment.getIsActive())
                        && employeeDepartment.getDepartment() != null
                        && Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive())) {
                    return employeeDepartment.getDepartment().getId();
                }
            }
            return null;
        }

        if (employee == null || employee.getEmployeeDepartments() == null) {
            return null;
        }

        for (EmployeeDepartment employeeDepartment : employee.getEmployeeDepartments()) {
            if (Boolean.TRUE.equals(employeeDepartment.getIsActive())
                    && employeeDepartment.getDepartment() != null
                    && Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive())) {
                return employeeDepartment.getDepartment().getId();
            }
        }

        return null;
    }

    private void validateDate(String value, String fieldKey, String messageKey, Map<String, String> errors) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        try {
            LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            errors.put(fieldKey, messageKey);
        }
    }

    private void updateSuperiorFromDto(SuperiorEditDto dto, Superior superior, Employee employee, Employee superiorEmployee) {
        superior.setEmployee(employee);
        superior.setSuperior(superiorEmployee);
        superior.setStartDate(parseDate(dto.getStartDate()));
        superior.setEndDate(parseDate(dto.getEndDate()));

        if (superior.getIsActive() == null) {
            superior.setIsActive(true);
        }
    }

    private SuperiorEditDto toDto(Superior superior) {
        SuperiorEditDto dto = new SuperiorEditDto();
        dto.setId(superior.getId());
        dto.setEmployeeId(superior.getEmployee() != null ? superior.getEmployee().getId() : null);
        dto.setSuperiorId(superior.getSuperior() != null ? superior.getSuperior().getId() : null);
        dto.setDepartmentId(getActiveDepartmentId(superior.getEmployee()));
        dto.setStartDate(formatDate(superior.getStartDate()));
        dto.setEndDate(formatDate(superior.getEndDate()));
        dto.setIsActive(superior.getIsActive());
        return dto;
    }

    private SuperiorListDto toListDto(Superior superior) {
        return toListDto(superior, null);
    }

    private SuperiorListDto toListDto(Superior superior, List<EmployeeDepartment> employeeDepartments) {
        SuperiorListDto dto = new SuperiorListDto();
        dto.setId(superior.getEmployee() != null ? superior.getEmployee().getId() : null);
        dto.setRelationId(superior.getId());
        dto.setSuperiorEmployeeId(superior.getSuperior() != null ? superior.getSuperior().getId() : null);
        dto.setEmployeeFullName(getEmployeeFullName(superior.getEmployee()));
        dto.setSuperiorFullName(getEmployeeFullName(superior.getSuperior()));
        dto.setDepartmentId(getActiveDepartmentId(superior.getEmployee(), employeeDepartments));
        dto.setDepartmentName(getActiveDepartmentName(superior.getEmployee(), employeeDepartments));
        dto.setStartDate(formatDate(superior.getStartDate()));
        dto.setEndDate(formatDate(superior.getEndDate()));
        dto.setIsActive(superior.getIsActive());
        return dto;
    }

    private SuperiorListDto toDepartmentHeadListDto(DepartmentHead departmentHead, List<Superior> superiorAssignments) {
        return toDepartmentHeadListDto(departmentHead, superiorAssignments, null);
    }

    private SuperiorListDto toDepartmentHeadListDto(DepartmentHead departmentHead, List<Superior> superiorAssignments, List<EmployeeDepartment> employeeDepartments) {
        SuperiorListDto dto = new SuperiorListDto();
        Integer superiorEmployeeId = departmentHead.getSuperior() != null ? departmentHead.getSuperior().getId() : null;
        Integer departmentId = departmentHead.getDepartment() != null ? departmentHead.getDepartment().getId() : null;

        dto.setId(superiorEmployeeId);
        dto.setDepartmentHeadId(departmentHead.getId());
        dto.setDepartmentId(departmentId);
        dto.setSuperiorEmployeeId(superiorEmployeeId);
        dto.setSuperiorFullName(getEmployeeFullName(departmentHead.getSuperior()));
        dto.setDepartmentName(departmentHead.getDepartment() != null ? departmentHead.getDepartment().getDepartmentName() : "");
        dto.setStartDate(formatDate(departmentHead.getStartDate()));
        dto.setEndDate(formatDate(departmentHead.getEndDate()));
        dto.setIsActive(departmentHead.getIsActive());
        dto.setManagedEmployeesCount(countManagedEmployees(superiorEmployeeId, departmentId, superiorAssignments, employeeDepartments));
        return dto;
    }

    private boolean isSameDepartmentHead(DepartmentHead departmentHead, Integer superiorEmployeeId, Integer departmentId) {
        return departmentHead != null
                && departmentHead.getSuperior() != null
                && departmentHead.getDepartment() != null
                && superiorEmployeeId.equals(departmentHead.getSuperior().getId())
                && departmentId.equals(departmentHead.getDepartment().getId());
    }

    private List<SuperiorListDto> buildSuperiorList(List<DepartmentHead> departmentHeads, List<Superior> superiorAssignments) {
        return buildSuperiorList(departmentHeads, superiorAssignments, null);
    }

    private List<SuperiorListDto> buildSuperiorList(List<DepartmentHead> departmentHeads, List<Superior> superiorAssignments, List<EmployeeDepartment> employeeDepartments) {
        List<SuperiorListDto> superiors = new ArrayList<>();
        Set<String> displayedKeys = new HashSet<>();

        if (departmentHeads != null) {
            departmentHeads.stream()
                    .filter(departmentHead -> departmentHead.getSuperior() != null)
                    .forEach(departmentHead -> {
                        SuperiorListDto dto = toDepartmentHeadListDto(departmentHead, superiorAssignments, employeeDepartments);
                        superiors.add(dto);
                        displayedKeys.add(buildSuperiorDepartmentKey(dto.getSuperiorEmployeeId(), dto.getDepartmentId()));
                    });
        }

        if (superiorAssignments != null) {
            superiorAssignments.stream()
                    .filter(superior -> Boolean.TRUE.equals(superior.getIsActive()))
                    .filter(superior -> superior.getSuperior() != null)
                    .forEach(superior -> {
                        Integer superiorEmployeeId = superior.getSuperior().getId();
                        Integer departmentId = getActiveDepartmentId(superior.getEmployee(), employeeDepartments);
                        String key = buildSuperiorDepartmentKey(superiorEmployeeId, departmentId);

                        if (departmentId != null && displayedKeys.add(key)) {
                            superiors.add(toLegacyDepartmentHeadListDto(superior, departmentId, superiorAssignments, employeeDepartments));
                        }
                    });
        }

        superiors.sort((left, right) -> {
            int superiorCompare = compareNullableStrings(left.getSuperiorFullName(), right.getSuperiorFullName());
            return superiorCompare != 0
                    ? superiorCompare
                    : compareNullableStrings(left.getDepartmentName(), right.getDepartmentName());
        });

        return superiors;
    }

    private List<SuperiorListDto> buildSuperiorList(List<DepartmentHead> departmentHeads, Map<String, Integer> managedEmployeeCounts) {
        List<SuperiorListDto> superiors = new ArrayList<>();

        if (departmentHeads != null) {
            departmentHeads.stream()
                    .filter(departmentHead -> departmentHead.getSuperior() != null)
                    .map(departmentHead -> toDepartmentHeadListDto(departmentHead, managedEmployeeCounts))
                    .forEach(superiors::add);
        }

        superiors.sort((left, right) -> {
            int superiorCompare = compareNullableStrings(left.getSuperiorFullName(), right.getSuperiorFullName());
            return superiorCompare != 0
                    ? superiorCompare
                    : compareNullableStrings(left.getDepartmentName(), right.getDepartmentName());
        });

        return superiors;
    }

    private SuperiorListDto toDepartmentHeadListDto(DepartmentHead departmentHead, Map<String, Integer> managedEmployeeCounts) {
        SuperiorListDto dto = new SuperiorListDto();
        Integer superiorEmployeeId = departmentHead.getSuperior() != null ? departmentHead.getSuperior().getId() : null;
        Integer departmentId = departmentHead.getDepartment() != null ? departmentHead.getDepartment().getId() : null;

        dto.setId(superiorEmployeeId);
        dto.setDepartmentHeadId(departmentHead.getId());
        dto.setDepartmentId(departmentId);
        dto.setSuperiorEmployeeId(superiorEmployeeId);
        dto.setSuperiorFullName(getEmployeeFullName(departmentHead.getSuperior()));
        dto.setDepartmentName(departmentHead.getDepartment() != null ? departmentHead.getDepartment().getDepartmentName() : "");
        dto.setStartDate(formatDate(departmentHead.getStartDate()));
        dto.setEndDate(formatDate(departmentHead.getEndDate()));
        dto.setIsActive(departmentHead.getIsActive());
        dto.setManagedEmployeesCount(getManagedEmployeeCount(superiorEmployeeId, departmentId, managedEmployeeCounts));
        return dto;
    }

    private Integer getManagedEmployeeCount(Integer superiorEmployeeId, Integer departmentId, Map<String, Integer> managedEmployeeCounts) {
        if (superiorEmployeeId == null || departmentId == null || managedEmployeeCounts == null) {
            return 0;
        }

        Integer count = managedEmployeeCounts.get(buildSuperiorDepartmentKey(superiorEmployeeId, departmentId));
        return count != null ? count : 0;
    }

    private SuperiorListDto toLegacyDepartmentHeadListDto(Superior superior, Integer departmentId, List<Superior> superiorAssignments, List<EmployeeDepartment> employeeDepartments) {
        SuperiorListDto dto = new SuperiorListDto();
        Integer superiorEmployeeId = superior.getSuperior() != null ? superior.getSuperior().getId() : null;

        dto.setId(superiorEmployeeId);
        dto.setDepartmentId(departmentId);
        dto.setSuperiorEmployeeId(superiorEmployeeId);
        dto.setSuperiorFullName(getEmployeeFullName(superior.getSuperior()));
        dto.setDepartmentName(getActiveDepartmentName(superior.getEmployee(), employeeDepartments));
        dto.setIsActive(true);
        dto.setManagedEmployeesCount(countManagedEmployees(superiorEmployeeId, departmentId, superiorAssignments, employeeDepartments));
        return dto;
    }

    private String buildSuperiorDepartmentKey(Integer superiorEmployeeId, Integer departmentId) {
        return String.valueOf(superiorEmployeeId) + ":" + String.valueOf(departmentId);
    }

    private int compareNullableStrings(String left, String right) {
        String safeLeft = left != null ? left : "";
        String safeRight = right != null ? right : "";
        return safeLeft.compareToIgnoreCase(safeRight);
    }

    private Integer countManagedEmployees(Integer superiorEmployeeId, Integer departmentId, List<Superior> superiorAssignments) {
        return countManagedEmployees(superiorEmployeeId, departmentId, superiorAssignments, null);
    }

    private Integer countManagedEmployees(Integer superiorEmployeeId, Integer departmentId, List<Superior> superiorAssignments, List<EmployeeDepartment> employeeDepartments) {
        if (superiorEmployeeId == null || departmentId == null || superiorAssignments == null) {
            return 0;
        }

        return (int) superiorAssignments.stream()
                .filter(assignment -> isActiveAssignmentForSuperiorAndDepartment(assignment, superiorEmployeeId, departmentId, employeeDepartments))
                .count();
    }

    private boolean isActiveAssignmentForDepartmentHead(DepartmentHead departmentHead, Superior assignment) {
        if (departmentHead == null || departmentHead.getSuperior() == null || departmentHead.getDepartment() == null) {
            return false;
        }

        return isActiveAssignmentForSuperiorAndDepartment(
                assignment,
                departmentHead.getSuperior().getId(),
                departmentHead.getDepartment().getId()
        );
    }

    private boolean isActiveAssignmentForSuperiorAndDepartment(Superior assignment, Integer superiorEmployeeId, Integer departmentId) {
        return isActiveAssignmentForSuperiorAndDepartment(assignment, superiorEmployeeId, departmentId, null);
    }

    private boolean isActiveAssignmentForSuperiorAndDepartment(Superior assignment, Integer superiorEmployeeId, Integer departmentId, List<EmployeeDepartment> employeeDepartments) {
        return assignment != null
                && Boolean.TRUE.equals(assignment.getIsActive())
                && assignment.getSuperior() != null
                && superiorEmployeeId.equals(assignment.getSuperior().getId())
                && departmentId.equals(getActiveDepartmentId(assignment.getEmployee(), employeeDepartments));
    }

    private String getEmployeeFullName(Employee employee) {
        if (employee == null) {
            return "";
        }

        String lastName = employee.getLastName() != null ? employee.getLastName() : "";
        String firstName = employee.getFirstName() != null ? employee.getFirstName() : "";
        return (lastName + " " + firstName).trim();
    }

    private String getActiveDepartmentName(Employee employee) {
        return getActiveDepartmentName(employee, null);
    }

    private String getActiveDepartmentName(Employee employee, List<EmployeeDepartment> employeeDepartments) {
        if (employee != null && employeeDepartments != null) {
            for (EmployeeDepartment employeeDepartment : employeeDepartments) {
                if (employeeDepartment.getEmployee() != null
                        && employee.getId() != null
                        && employee.getId().equals(employeeDepartment.getEmployee().getId())
                        && Boolean.TRUE.equals(employeeDepartment.getIsActive())
                        && employeeDepartment.getDepartment() != null
                        && Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive())) {
                    return employeeDepartment.getDepartment().getDepartmentName();
                }
            }
            return "";
        }

        if (employee == null || employee.getEmployeeDepartments() == null) {
            return "";
        }

        for (EmployeeDepartment employeeDepartment : employee.getEmployeeDepartments()) {
            if (Boolean.TRUE.equals(employeeDepartment.getIsActive())
                    && employeeDepartment.getDepartment() != null
                    && Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive())) {
                return employeeDepartment.getDepartment().getDepartmentName();
            }
        }

        return "";
    }

    private LocalDate parseDate(String value) {
        return value == null || value.trim().isEmpty() ? null : LocalDate.parse(value.trim());
    }

    private String formatDate(LocalDate value) {
        return value == null ? "" : value.toString();
    }
}
