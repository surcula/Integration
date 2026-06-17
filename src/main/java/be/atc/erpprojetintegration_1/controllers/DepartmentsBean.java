package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.DepartmentBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class DepartmentsBean implements Serializable {

    private static final Logger log = Logger.getLogger(DepartmentsBean.class);

    @Inject
    private DepartmentBusiness departmentBusiness;

    @Inject
    private EmployeeBusiness employeeBusiness;

    @Inject
    private AuthBean authBean;

    private List<Department> departments;
    private List<EmployeeListDto> managedEmployees = new ArrayList<>();
    private boolean showManagedEmployees;

    @PostConstruct
    public void init() {
        loadDepartments();
        loadManagedEmployees();
    }

    public void changeDepartmentActiveStatus(Integer id, boolean active) {
        Result<Void> result = departmentBusiness.setActive(id, !active);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, active ? "departments.delete.error" : "departments.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(active ? "departments.delete.success" : "departments.activate.success");
        loadDepartments();
        loadManagedEmployees();
    }

    private void loadDepartments() {
        Result<List<Department>> result = departmentBusiness.getAllDepartments();

        if (result.isSuccess()) {
            departments = result.getData();
            log.info("Departments loaded in list page: " + departments.size());
        } else {
            departments = new ArrayList<>();
            log.warn("Department list loading failed");
            MessageUtils.addErrorMessages(result, "departments.error.load");
        }
    }


    private void loadManagedEmployees() {
        showManagedEmployees = false;
        managedEmployees = new ArrayList<>();

        if (authBean.isHrOrAdmin() || authBean.getConnectedEmployee() == null) {
            return;
        }

        Integer connectedEmployeeId = authBean.getConnectedEmployee().getId();
        Result<Boolean> headResult = employeeBusiness.isDepartmentHead(connectedEmployeeId);
        if (!headResult.isSuccess() || !Boolean.TRUE.equals(headResult.getData())) {
            return;
        }

        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeListForViewer(
                connectedEmployeeId, false, false);
        if (result.isSuccess()) {
            managedEmployees = result.getData();
            showManagedEmployees = true;
        } else {
            log.warn("Managed employee list loading failed");
            MessageUtils.addErrorMessages(result, "employees.error.load");
        }
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public List<EmployeeListDto> getManagedEmployees() { return managedEmployees; }
    public boolean isShowManagedEmployees() { return showManagedEmployees; }
}
