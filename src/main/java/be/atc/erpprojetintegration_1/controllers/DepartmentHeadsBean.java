package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.DepartmentHeadBusiness;
import be.atc.erpprojetintegration_1.dto.DepartmentHeadRowDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.Employee;
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
import java.util.List;

@Named
@ViewScoped
public class DepartmentHeadsBean implements Serializable {
    @Inject private DepartmentHeadBusiness departmentHeadBusiness;
    @Inject private AuthBean authBean;

    private List<DepartmentHeadRowDto> rows = new ArrayList<>();
    private List<Department> departments = new ArrayList<>();
    private List<DepartmentHead> allHeads = new ArrayList<>();
    private List<Employee> eligibleEmployees = new ArrayList<>();
    private List<DepartmentHead> selectedHistory = new ArrayList<>();
    private DepartmentHead selectedHead;
    private Department selectedDepartment;
    private Integer departmentId;
    private Integer employeeId;
    private Integer selectedHeadId;
    private LocalDate startDate;
    private LocalDate endDate;

    @PostConstruct
    public void init() {
        if (!isCanEditDepartmentHeads()) return;
        load();
    }

    public void prepareAssign(Department department) {
        selectedDepartment = department;
        departmentId = department.getId();
        employeeId = null;
        startDate = LocalDate.now();
        loadEligibleEmployees();
    }

    public void loadEligibleEmployees() {
        Result<List<Employee>> result = departmentHeadBusiness.getEligibleEmployees(departmentId);
        eligibleEmployees = result.isSuccess() ? result.getData() : new ArrayList<Employee>();
    }

    public void prepareView(DepartmentHead head) { selectedHead = head; }

    public void prepareHistory(Department department) {
        selectedHistory = new ArrayList<>();
        for (DepartmentHead head : allHeads) {
            if (department.getId().equals(head.getDepartment().getId()) && !Boolean.TRUE.equals(head.getIsActive())) {
                selectedHistory.add(head);
            }
        }
        selectedHistory.sort(Comparator.comparing(DepartmentHead::getStartDate,
                Comparator.nullsLast(Comparator.reverseOrder())));
    }

    public void prepareDeactivate(Integer id) {
        selectedHeadId = id;
        endDate = LocalDate.now();
    }

    public void assign() {
        if (!isCanEditDepartmentHeads()) { error("Vous n'etes pas autorise a modifier les chefs de departement."); return; }
        Result<DepartmentHead> result = departmentHeadBusiness.assign(departmentId, employeeId, startDate);
        if (!result.isSuccess()) { error(firstError(result)); return; }
        load();
        info("Chef de departement enregistre.");
    }

    public void deactivate() {
        if (!isCanEditDepartmentHeads()) { error("Vous n'etes pas autorise a modifier les chefs de departement."); return; }
        Result<Void> result = departmentHeadBusiness.deactivate(selectedHeadId, endDate);
        if (!result.isSuccess()) { error(firstError(result)); return; }
        load();
        info("Mandat termine.");
    }

    private void load() {
        Result<List<Department>> departmentResult = departmentHeadBusiness.getActiveDepartments();
        departments = departmentResult.isSuccess() ? departmentResult.getData() : new ArrayList<Department>();
        Result<List<DepartmentHead>> headResult = departmentHeadBusiness.getAll();
        allHeads = headResult.isSuccess() ? headResult.getData() : new ArrayList<DepartmentHead>();
        buildRows();
    }

    private void buildRows() {
        rows = new ArrayList<>();
        for (Department department : departments) {
            DepartmentHead current = null;
            int historyCount = 0;
            for (DepartmentHead head : allHeads) {
                if (!department.getId().equals(head.getDepartment().getId())) continue;
                if (Boolean.TRUE.equals(head.getIsActive())) current = head;
                else historyCount++;
            }
            rows.add(new DepartmentHeadRowDto(department, current, historyCount));
        }
    }

    private String firstError(Result<?> result) {
        return result.getErrors() == null || result.getErrors().isEmpty()
                ? "Operation impossible." : result.getErrors().values().iterator().next();
    }

    private void info(String message) { FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Succes", message)); }
    private void error(String message) { FacesContext.getCurrentInstance().validationFailed(); FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", message)); }

    public boolean isCanEditDepartmentHeads() { return authBean != null && authBean.isHrOrAdmin(); }
    public List<DepartmentHeadRowDto> getRows() { return rows; }
    public List<Employee> getEligibleEmployees() { return eligibleEmployees; }
    public List<DepartmentHead> getSelectedHistory() { return selectedHistory; }
    public DepartmentHead getSelectedHead() { return selectedHead; }
    public Department getSelectedDepartment() { return selectedDepartment; }
    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}
