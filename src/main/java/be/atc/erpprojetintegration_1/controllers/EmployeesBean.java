package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

@Named
@RequestScoped
public class EmployeesBean implements Serializable {
    private static final Logger log = Logger.getLogger(EmployeesBean.class);

    @Inject
    private EmployeeBusiness employeeBusiness;
    @Inject
    private AuthBean authBean;

    private List<EmployeeListDto> employees;
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
        boolean globalAccess = authBean.isHrOrAdmin();
        canViewEmployeeList = globalAccess;
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
        return employees;
    }

    public boolean isCanViewEmployeeList() { return canViewEmployeeList; }
}
