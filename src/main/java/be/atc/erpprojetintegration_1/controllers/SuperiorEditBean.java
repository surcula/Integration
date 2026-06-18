package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.SuperiorBusiness;
import be.atc.erpprojetintegration_1.dto.SuperiorEditDto;
import be.atc.erpprojetintegration_1.dto.SuperiorListDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;
import org.primefaces.model.DualListModel;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class SuperiorEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(SuperiorEditBean.class);

    @Inject
    private SuperiorBusiness superiorBusiness;

    @Inject
    private AuthBean authBean;

    private Integer superiorId;
    private Integer departmentId;
    private SuperiorEditDto superior;
    private Employee selectedSuperior;
    private List<Employee> employees;
    private List<Department> departments;
    private List<Employee> availableSuperiors;
    private List<Employee> availableEmployees;
    private List<SuperiorListDto> assignedEmployees;
    private Integer selectedEmployeeId;
    private DualListModel<Employee> employeePickList;

    /**
     * Loads the superior assignment form and employee choices.
     */
    public String loadSuperior() {
        if (!hasFormAccess()) {
            MessageUtils.addWarningMessage("common.accessDenied");
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            return "/hub?faces-redirect=true";
        }

        loadEmployees();
        loadDepartments();

        if (superiorId == null) {
            superior = new SuperiorEditDto();
            superior.setIsActive(true);
            selectedSuperior = null;
            assignedEmployees = new ArrayList<>();
            availableEmployees = new ArrayList<>();
            employeePickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
            return null;
        }

        superior = new SuperiorEditDto();
        superior.setSuperiorId(superiorId);
        superior.setDepartmentId(departmentId);
        superior.setIsActive(true);
        selectedSuperior = findEmployee(superiorId);
        refreshTeam();
        log.info("Superior team edit page loaded for employee id: " + superiorId);
        return null;
    }

    /**
     * Saves the superior assignment and redirects to the list page.
     *
     * @return JSF navigation outcome
     */
    public String save() {
        if (!hasFormAccess()) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return null;
        }

        syncSelectedSuperior();
        syncSelectedSuperiorDepartment();

        Result<Void> departmentHeadResult = superiorBusiness.createDepartmentHeadIfNeeded(
                superior.getSuperiorId(),
                superior.getDepartmentId()
        );

        if (!departmentHeadResult.isSuccess()) {
            MessageUtils.addErrorMessages(departmentHeadResult, "superiors.departmentHead.save.error");
            return null;
        }

        Result<Void> result = saveTeamAssignments();

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "superiors.error.save");
            return null;
        }

        MessageUtils.addInfoMessage("superiors.team.save.success");
        selectedEmployeeId = null;
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/superiors?faces-redirect=true";
    }

    /**
     * Removes an employee from the selected superior team.
     *
     * @param relationId assignment relation id
     */
    public void removeEmployee(Integer relationId) {
        if (!authBean.hasPermission("superior:edit")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return;
        }

        Result<Void> result = superiorBusiness.removeEmployeeFromSuperior(relationId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "superiors.employee.remove.error");
            return;
        }

        MessageUtils.addInfoMessage("superiors.employee.remove.success");
        refreshTeam();
    }

    public boolean isCreateMode() {
        return superiorId == null;
    }

    private boolean hasFormAccess() {
        return isCreateMode()
                ? authBean.hasPermission("superior:create")
                : authBean.hasPermission("superior:edit");
    }

    private void loadEmployees() {
        Result<List<Employee>> result = superiorBusiness.getActiveEmployees();

        if (result.isSuccess()) {
            employees = result.getData();
            availableSuperiors = new ArrayList<>();
            availableEmployees = new ArrayList<>();
            assignedEmployees = new ArrayList<>();
            employeePickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
            return;
        }

        employees = new ArrayList<>();
        availableSuperiors = new ArrayList<>();
        availableEmployees = new ArrayList<>();
        assignedEmployees = new ArrayList<>();
        employeePickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
        MessageUtils.addErrorMessages(result, "employees.error.load");
    }

    private void loadDepartments() {
        Result<List<Department>> result = superiorBusiness.getActiveDepartments();

        if (result.isSuccess()) {
            departments = result.getData();
            return;
        }

        departments = new ArrayList<>();
        MessageUtils.addErrorMessages(result, "departments.error.load");
    }

    /**
     * Refreshes the team when the superior selection changes.
     */
    public void onSuperiorChanged() {
        syncSelectedSuperior();
        syncSelectedSuperiorDepartment();
        selectedEmployeeId = null;
        refreshTeam();
    }

    private void refreshTeam() {
        if (superior == null || superior.getSuperiorId() == null || superior.getDepartmentId() == null) {
            availableEmployees = new ArrayList<>();
            assignedEmployees = new ArrayList<>();
            employeePickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
            return;
        }

        Result<List<SuperiorListDto>> assignedResult = superiorBusiness.getEmployeesBySuperior(
                superior.getSuperiorId(),
                superior.getDepartmentId()
        );

        if (assignedResult.isSuccess()) {
            assignedEmployees = assignedResult.getData();
        } else {
            assignedEmployees = new ArrayList<>();
            MessageUtils.addErrorMessages(assignedResult, "superiors.error.load");
        }

        Result<List<Employee>> availableResult = superiorBusiness.getAvailableEmployeesForSuperior(
                superior.getSuperiorId(),
                superior.getDepartmentId()
        );

        if (availableResult.isSuccess()) {
            availableEmployees = availableResult.getData();
        } else {
            availableEmployees = new ArrayList<>();
            MessageUtils.addErrorMessages(availableResult, "superiors.error.load");
        }

        employeePickList = new DualListModel<>(availableEmployees, getAssignedEmployeeEntities());
    }

    private Result<Void> saveTeamAssignments() {
        List<Integer> currentEmployeeIds = assignedEmployees.stream()
                .map(SuperiorListDto::getId)
                .collect(Collectors.toList());

        List<Integer> targetEmployeeIds = employeePickList.getTarget().stream()
                .map(Employee::getId)
                .collect(Collectors.toList());

        for (Integer employeeId : targetEmployeeIds) {
            if (!currentEmployeeIds.contains(employeeId)) {
                Result<Void> addResult = superiorBusiness.addEmployeeToSuperior(superior.getSuperiorId(), employeeId);

                if (!addResult.isSuccess()) {
                    return addResult;
                }
            }
        }

        for (SuperiorListDto assignedEmployee : assignedEmployees) {
            if (!targetEmployeeIds.contains(assignedEmployee.getId())) {
                Result<Void> removeResult = superiorBusiness.removeEmployeeFromSuperior(assignedEmployee.getRelationId());

                if (!removeResult.isSuccess()) {
                    return removeResult;
                }
            }
        }

        return Result.ok();
    }

    private List<Employee> getAssignedEmployeeEntities() {
        List<Integer> assignedEmployeeIds = assignedEmployees.stream()
                .map(SuperiorListDto::getId)
                .collect(Collectors.toList());

        return employees.stream()
                .filter(employee -> assignedEmployeeIds.contains(employee.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Returns active employees matching the typed query for superior selection.
     *
     * @param query typed search text
     * @return matching employees
     */
    public List<Employee> completeSuperior(String query) {
        String search = query == null ? "" : query.trim().toLowerCase();

        return employees.stream()
                .filter(employee -> getEmployeeFullName(employee).toLowerCase().contains(search)
                        || (employee.getEmail() != null && employee.getEmail().toLowerCase().contains(search)))
                .limit(25)
                .collect(Collectors.toList());
    }

    private Employee findEmployee(Integer employeeId) {
        if (employeeId == null || employees == null) {
            return null;
        }

        return employees.stream()
                .filter(employee -> employeeId.equals(employee.getId()))
                .findFirst()
                .orElse(null);
    }

    private void syncSelectedSuperior() {
        superior.setSuperiorId(selectedSuperior != null ? selectedSuperior.getId() : null);
    }

    private void syncSelectedSuperiorDepartment() {
        superior.setDepartmentId(selectedSuperior != null ? getActiveDepartmentId(selectedSuperior) : null);
    }

    public String getEmployeeFullName(Employee employee) {
        if (employee == null) {
            return "";
        }

        return (employee.getLastName() + " " + employee.getFirstName()).trim();
    }

    public String getSelectedSuperiorDepartmentName() {
        return selectedSuperior == null ? "" : getActiveDepartmentName(selectedSuperior);
    }

    private Integer getActiveDepartmentId(Employee employee) {
        if (employee == null || employee.getEmployeeDepartments() == null) {
            return null;
        }

        return employee.getEmployeeDepartments()
                .stream()
                .filter(employeeDepartment -> Boolean.TRUE.equals(employeeDepartment.getIsActive()))
                .filter(employeeDepartment -> employeeDepartment.getDepartment() != null)
                .filter(employeeDepartment -> Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive()))
                .map(employeeDepartment -> employeeDepartment.getDepartment().getId())
                .findFirst()
                .orElse(null);
    }

    private String getActiveDepartmentName(Employee employee) {
        if (employee == null || employee.getEmployeeDepartments() == null) {
            return "";
        }

        return employee.getEmployeeDepartments()
                .stream()
                .filter(employeeDepartment -> Boolean.TRUE.equals(employeeDepartment.getIsActive()))
                .filter(employeeDepartment -> employeeDepartment.getDepartment() != null)
                .filter(employeeDepartment -> Boolean.TRUE.equals(employeeDepartment.getDepartment().getIsActive()))
                .map(employeeDepartment -> employeeDepartment.getDepartment().getDepartmentName())
                .findFirst()
                .orElse("");
    }

    public Integer getSuperiorId() {
        return superiorId;
    }

    public void setSuperiorId(Integer superiorId) {
        this.superiorId = superiorId;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public SuperiorEditDto getSuperior() {
        return superior;
    }

    public Employee getSelectedSuperior() {
        return selectedSuperior;
    }

    public void setSelectedSuperior(Employee selectedSuperior) {
        this.selectedSuperior = selectedSuperior;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public List<Employee> getAvailableSuperiors() {
        return employees;
    }

    public List<Employee> getAvailableEmployees() {
        return availableEmployees;
    }

    public List<SuperiorListDto> getAssignedEmployees() {
        return assignedEmployees;
    }

    public Integer getSelectedEmployeeId() {
        return selectedEmployeeId;
    }

    public void setSelectedEmployeeId(Integer selectedEmployeeId) {
        this.selectedEmployeeId = selectedEmployeeId;
    }

    public DualListModel<Employee> getEmployeePickList() {
        return employeePickList;
    }

    public void setEmployeePickList(DualListModel<Employee> employeePickList) {
        this.employeePickList = employeePickList;
    }
}
