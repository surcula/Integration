package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.SuperiorBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeDetailsDto;
import be.atc.erpprojetintegration_1.dto.SuperiorListDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class SuperiorsBean implements Serializable {

    private static final Logger log = Logger.getLogger(SuperiorsBean.class);

    @Inject
    private SuperiorBusiness superiorBusiness;

    @Inject
    private EmployeeBusiness employeeBusiness;

    @Inject
    private AuthBean authBean;

    private List<SuperiorListDto> superiors;
    private String selectedDepartmentName;
    private EmployeeDetailsDto selectedEmployeeDetails;

    @PostConstruct
    public void init() {
        loadSuperiors();
    }

    /**
     * Changes a superior assignment active status and reloads the list.
     *
     * @param id superior assignment id
     * @param active current active status
     */
    public void changeSuperiorActiveStatus(Integer id, boolean active) {
        Result<Void> result = superiorBusiness.setActive(id, !active);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, active ? "superiors.delete.error" : "superiors.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(active ? "superiors.delete.success" : "superiors.activate.success");
        loadSuperiors();
    }

    /**
     * Removes a department head and reloads the list.
     *
     * @param departmentHeadId department head id
     */
    public void removeDepartmentHead(Integer departmentHeadId) {
        Result<Void> result = superiorBusiness.removeDepartmentHead(departmentHeadId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "superiors.departmentHead.delete.error");
            return;
        }

        MessageUtils.addInfoMessage("superiors.departmentHead.delete.success");
        loadSuperiors();
    }

    public void changeDepartmentHeadActiveStatus(Integer departmentHeadId, boolean active) {
        Result<Void> result = superiorBusiness.setDepartmentHeadActive(departmentHeadId, !active);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, active ? "superiors.delete.error" : "superiors.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(active ? "superiors.delete.success" : "superiors.activate.success");
        loadSuperiors();
    }

    /**
     * Removes all employees from a superior team without removing the department head.
     *
     * @param superiorEmployeeId superior employee id
     * @param departmentId department id
     */
    public void removeTeam(Integer superiorEmployeeId, Integer departmentId) {
        Result<Void> result = superiorBusiness.removeTeam(superiorEmployeeId, departmentId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "superiors.team.delete.error");
            return;
        }

        MessageUtils.addInfoMessage("superiors.team.delete.success");
        loadSuperiors();
    }

    /**
     * Loads the selected superior's employee information and opens the detail dialog.
     *
     * @param employeeId superior employee identifier
     */
    public void showEmployeeDetails(Integer employeeId) {
        selectedEmployeeDetails = null;

        if (!authBean.hasPermission("employee:read")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return;
        }

        Result<EmployeeDetailsDto> result = employeeBusiness.getEmployeeDetails(employeeId);
        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "employees.details.error.load");
            return;
        }

        selectedEmployeeDetails = result.getData();
        PrimeFaces.current().executeScript("PF('superiorDetailsDialog').show()");
    }

    /**
     * Activates or deactivates the employee displayed in the superior detail dialog.
     *
     * @param employeeId employee identifier
     * @param active current active status
     */
    public void changeEmployeeActiveStatus(Integer employeeId, boolean active) {
        if (!authBean.hasPermission("employee:delete")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return;
        }

        Result<Void> result = active
                ? employeeBusiness.deactivateEmployee(employeeId)
                : employeeBusiness.activateEmployee(employeeId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result,
                    active ? "employee.delete.error" : "employee.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(
                active ? "employee.delete.success" : "employee.activate.success");
        selectedEmployeeDetails = null;
        loadSuperiors();
    }

    /**
     * Clears the department filter and reloads the displayed list.
     */
    public void clearDepartmentFilter() {
        selectedDepartmentName = null;
    }

    private void loadSuperiors() {
        Result<List<SuperiorListDto>> result = superiorBusiness.getAllSuperiors();

        if (result.isSuccess()) {
            superiors = result.getData();
            log.info("Superior assignments loaded in list page: " + superiors.size());
        } else {
            superiors = new ArrayList<>();
            log.warn("Superior assignment list loading failed");
            MessageUtils.addErrorMessages(result, "superiors.error.load");
        }
    }

    public List<SuperiorListDto> getSuperiors() {
        return superiors;
    }

    public List<SuperiorListDto> getFilteredSuperiors() {
        if (selectedDepartmentName == null || selectedDepartmentName.trim().isEmpty()) {
            return superiors;
        }

        return superiors.stream()
                .filter(superior -> selectedDepartmentName.equals(superior.getDepartmentName()))
                .collect(Collectors.toList());
    }

    public List<String> getDepartmentNames() {
        if (superiors == null) {
            return Collections.emptyList();
        }

        return superiors.stream()
                .map(SuperiorListDto::getDepartmentName)
                .filter(departmentName -> departmentName != null && !departmentName.trim().isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public String getSelectedDepartmentName() {
        return selectedDepartmentName;
    }

    public void setSelectedDepartmentName(String selectedDepartmentName) {
        this.selectedDepartmentName = selectedDepartmentName;
    }

    public EmployeeDetailsDto getSelectedEmployeeDetails() {
        return selectedEmployeeDetails;
    }
}
