package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeDepartmentBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeDepartmentRowDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.EmployeeDepartment;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class EmployeeDepartmentsBean implements Serializable {
    @Inject private EmployeeDepartmentBusiness employeeDepartmentBusiness;
    @Inject private AuthBean authBean;

    private List<EmployeeDepartmentRowDto> assignmentRows = new ArrayList<>();
    private List<EmployeeDepartment> allAssignments = new ArrayList<>();
    private List<Employee> employees = new ArrayList<>();
    private List<Department> departments = new ArrayList<>();
    private Integer employeeId;
    private Integer departmentId;
    private LocalDate startDate;
    private Integer selectedAssignmentId;
    private LocalDate endDate;
    private String searchTerm;
    private Integer filterDepartmentId;
    private String filterStatus = "ALL";
    private EmployeeDepartment selectedAssignment;
    private List<EmployeeDepartment> selectedHistory = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (!isCanManage()) return;
        startDate = LocalDate.now();
        load();
    }

    public void prepareCreate() {
        employeeId = null;
        departmentId = null;
        startDate = LocalDate.now();
    }

    public void prepareDeactivate(Integer id) {
        selectedAssignmentId = id;
        endDate = LocalDate.now();
    }

    public void prepareView(EmployeeDepartment assignment) {
        selectedAssignment = assignment;
    }

    public void prepareHistory(EmployeeDepartment assignment) {
        selectedAssignment = assignment;
        selectedHistory = new ArrayList<>();
        for (EmployeeDepartment item : allAssignments) {
            if (item.getEmployee().getId().equals(assignment.getEmployee().getId())
                    && !item.getId().equals(assignment.getId())) {
                selectedHistory.add(item);
            }
        }
        selectedHistory.sort(Comparator.comparing(EmployeeDepartment::getStartDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
    }

    public void applyFilters() {
        List<EmployeeDepartment> sortedAssignments = new ArrayList<>(allAssignments);
        sortedAssignments.sort(Comparator
                .comparing((EmployeeDepartment assignment) -> value(assignment.getEmployee().getLastName()))
                .thenComparing(assignment -> value(assignment.getEmployee().getFirstName()))
                .thenComparing(assignment -> !Boolean.TRUE.equals(assignment.getIsActive()))
                .thenComparing(EmployeeDepartment::getStartDate, Comparator.nullsLast(Comparator.reverseOrder())));

        Map<Integer, EmployeeDepartment> displayedByEmployee = new LinkedHashMap<>();
        for (EmployeeDepartment assignment : sortedAssignments) {
            displayedByEmployee.putIfAbsent(assignment.getEmployee().getId(), assignment);
        }

        assignmentRows = new ArrayList<>();
        String search = searchTerm == null ? "" : searchTerm.trim().toLowerCase();
        for (EmployeeDepartment assignment : displayedByEmployee.values()) {
            boolean matchesSearch = search.isEmpty()
                    || value(assignment.getEmployee().getFirstName()).contains(search)
                    || value(assignment.getEmployee().getLastName()).contains(search)
                    || value(assignment.getEmployee().getEmail()).contains(search);
            boolean matchesDepartment = filterDepartmentId == null
                    || filterDepartmentId.equals(assignment.getDepartment().getId());
            boolean matchesStatus = "ALL".equals(filterStatus)
                    || ("ACTIVE".equals(filterStatus) && Boolean.TRUE.equals(assignment.getIsActive()))
                    || ("ENDED".equals(filterStatus) && !Boolean.TRUE.equals(assignment.getIsActive()));
            if (!matchesSearch || !matchesDepartment || !matchesStatus) continue;
            int historyCount = 0;
            for (EmployeeDepartment item : allAssignments) {
                if (item.getEmployee().getId().equals(assignment.getEmployee().getId())
                        && !item.getId().equals(assignment.getId())) {
                    historyCount++;
                }
            }
            assignmentRows.add(new EmployeeDepartmentRowDto(assignment, historyCount));
        }
    }

    public void assign() {
        if (!isCanManage()) { error("Vous n'etes pas autorise a modifier les affectations."); return; }
        Result<EmployeeDepartment> result = employeeDepartmentBusiness.assign(employeeId, departmentId, startDate);
        if (!result.isSuccess()) {
            error(firstError(result));
            return;
        }
        loadAssignments();
        info("Affectation enregistree. L'ancienne affectation a ete cloturee si necessaire.");
    }

    public void deactivate() {
        if (!isCanManage()) { error("Vous n'etes pas autorise a modifier les affectations."); return; }
        Result<Void> result = employeeDepartmentBusiness.deactivate(selectedAssignmentId, endDate);
        if (!result.isSuccess()) {
            error(firstError(result));
            return;
        }
        loadAssignments();
        info("Affectation terminee.");
    }

    private void load() {
        loadAssignments();
        Result<List<Employee>> employeeResult = employeeDepartmentBusiness.getActiveEmployees();
        if (employeeResult.isSuccess()) employees = employeeResult.getData();
        Result<List<Department>> departmentResult = employeeDepartmentBusiness.getActiveDepartments();
        if (departmentResult.isSuccess()) departments = departmentResult.getData();
    }

    private void loadAssignments() {
        Result<List<EmployeeDepartment>> result = employeeDepartmentBusiness.getHistory();
        allAssignments = result.isSuccess() ? result.getData() : new ArrayList<EmployeeDepartment>();
        applyFilters();
    }

    private String value(String value) { return value == null ? "" : value.toLowerCase(); }

    private String firstError(Result<?> result) {
        return result.getErrors() == null || result.getErrors().isEmpty()
                ? "Operation impossible." : result.getErrors().values().iterator().next();
    }

    private void info(String message) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", message)); }
    private void error(String message) { FacesContext.getCurrentInstance().validationFailed(); FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message)); }

    public boolean isCanManage() { return authBean != null && authBean.isHrOrAdmin(); }

    public List<EmployeeDepartmentRowDto> getAssignmentRows() { return assignmentRows; }
    public List<Employee> getEmployees() { return employees; }
    public List<Department> getDepartments() { return departments; }
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getSearchTerm() { return searchTerm; }
    public void setSearchTerm(String searchTerm) { this.searchTerm = searchTerm; }
    public Integer getFilterDepartmentId() { return filterDepartmentId; }
    public void setFilterDepartmentId(Integer filterDepartmentId) { this.filterDepartmentId = filterDepartmentId; }
    public String getFilterStatus() { return filterStatus; }
    public void setFilterStatus(String filterStatus) { this.filterStatus = filterStatus; }
    public EmployeeDepartment getSelectedAssignment() { return selectedAssignment; }
    public List<EmployeeDepartment> getSelectedHistory() { return selectedHistory; }
}
