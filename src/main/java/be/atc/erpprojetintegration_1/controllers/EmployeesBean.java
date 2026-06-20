package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.dto.EmployeeDetailsDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

@Named
@RequestScoped
public class EmployeesBean implements Serializable {
    private static final Logger log = Logger.getLogger(EmployeesBean.class);

    @Inject
    private EmployeeBusiness employeeBusiness;
    @Inject
    private AuthBean authBean;

    private List<EmployeeListDto> employees;
    private String searchTerm;
    private String temporaryPassword;
    private EmployeeDetailsDto selectedEmployeeDetails;
    private boolean canViewEmployeeList;

    /**
     * Loads employees when the page is initialized.
     */
    @PostConstruct
    public void init() {
        loadEmployees();
    }

    /**
     * Deactivates an employee from the employee list.
     *
     * @param employeeId employee id
     */
    public void deactivateEmployee(Integer employeeId) {
        if (!authBean.hasPermission("employee:delete")) return;
        log.info("Deactivation requested for employee id: " + employeeId);
        Result<Void> result = employeeBusiness.deactivateEmployee(employeeId);

        if (result.isSuccess()) {
            log.info("Employee deactivated from list page with id: " + employeeId);
            MessageUtils.addInfoMessage("employee.delete.success");
            loadEmployees();
        } else {
            log.warn("Employee deactivation failed for id: " + employeeId);
            MessageUtils.addErrorMessages(result, "employee.delete.error");
        }
    }

    /**
     * Activates or deactivates an employee according to their current status.
     *
     * @param employeeId employee id
     * @param active current employee status
     */
    public void changeEmployeeActiveStatus(Integer employeeId, boolean active) {
        if (!authBean.hasPermission("employee:delete")) return;
        log.info((active ? "Deactivation" : "Activation") + " requested for employee id: " + employeeId);
        Result<Void> result = active
                ? employeeBusiness.deactivateEmployee(employeeId)
                : employeeBusiness.activateEmployee(employeeId);

        if (result.isSuccess()) {
            log.info("Employee active status changed for id: " + employeeId);
            MessageUtils.addInfoMessage(active ? "employee.delete.success" : "employee.activate.success");
            loadEmployees();
        } else {
            log.warn("Employee active status change failed for id: " + employeeId);
            MessageUtils.addErrorMessages(result, active ? "employee.delete.error" : "employee.activate.error");
        }
    }

    /**
     * Loads the employee list and includes inactive employees when the user has delete permission.
     */
    private void loadEmployees() {
        canViewEmployeeList = authBean.hasPermission("employee:read");
        employees = new ArrayList<>();

        if (!canViewEmployeeList) {
            return;
        }

        boolean includeInactive = authBean.hasPermission("employee:delete");
        log.info("Loading employee list. Include inactive: " + includeInactive);

        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeList(includeInactive);

        if (result.isSuccess()) {
            employees = result.getData();
            log.info("Employee list loaded: " + employees.size() + " employee(s)");
        } else {
            employees = new ArrayList<>();
            canViewEmployeeList = false;
            log.warn("Employee list loading failed");
            MessageUtils.addErrorMessages(result, "employees.error.load");
        }
    }

    /**
     * Returns employees displayed by the list page.
     *
     * @return employee list
     */
    public List<EmployeeListDto> getEmployees() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return employees;
        }

        String normalizedSearch = searchTerm.trim().toLowerCase(Locale.ROOT);

        return employees.stream()
                .filter(employee -> contains(employee.getFullName(), normalizedSearch)
                        || contains(employee.getDepartmentName(), normalizedSearch)
                        || contains(employee.getPhone(), normalizedSearch)
                        || contains(employee.getEmail(), normalizedSearch))
                .collect(Collectors.toList());
    }

    /**
     * Resets an employee password and displays the generated temporary password once.
     *
     * @param employeeId employee identifier
     */
    public void resetPassword(Integer employeeId) {
        temporaryPassword = null;

        if (!authBean.hasPermission("employee:reset-password")) {
            log.warn("Unauthorized employee password reset attempt");
            MessageUtils.addErrorMessage("common.accessDenied");
            return;
        }

        if (authBean.getConnectedEmployee() != null
                && employeeId != null
                && employeeId.equals(authBean.getConnectedEmployee().getId())) {
            MessageUtils.addErrorMessage("employee.resetPassword.error.self");
            return;
        }

        log.info("Password reset requested for employee id: " + employeeId);
        Result<String> result = employeeBusiness.resetPassword(employeeId);

        if (!result.isSuccess()) {
            log.warn("Password reset failed for employee id: " + employeeId);
            MessageUtils.addErrorMessages(result, "employee.resetPassword.error");
            return;
        }

        temporaryPassword = result.getData();
        MessageUtils.addInfoMessage("employee.resetPassword.success");
        PrimeFaces.current().executeScript("PF('temporaryPasswordDialog').show()");
    }

    /**
     * Loads an employee's read-only details and opens the detail dialog.
     *
     * @param employeeId employee identifier
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
        PrimeFaces.current().executeScript("PF('employeeDetailsDialog').show()");
    }

    /**
     * Checks if a value contains the current search term.
     *
     * @param value value to inspect
     * @param search normalized search term
     * @return true when the value contains the search term
     */
    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    /**
     * Returns the current search term used by the employee list.
     *
     * @return search term
     */
    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * Sets the current search term used by the employee list.
     *
     * @param searchTerm search term
     */
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    /**
     * Returns the complete employee list without search filtering.
     *
     * @return complete employee list
     */
    public List<EmployeeListDto> getAllEmployees() {
        return employees;
    }

    public boolean isCanViewEmployeeList() { return canViewEmployeeList; }

    public String getTemporaryPassword() {
        return temporaryPassword;
    }

    public EmployeeDetailsDto getSelectedEmployeeDetails() {
        return selectedEmployeeDetails;
    }
}
