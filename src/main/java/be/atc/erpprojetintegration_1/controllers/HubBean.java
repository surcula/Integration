package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.DepartmentBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.business.EvaluationBusiness;
import be.atc.erpprojetintegration_1.business.FunctionBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class HubBean implements Serializable {

    private static final Logger log = Logger.getLogger(HubBean.class);

    @Inject
    private EmployeeBusiness employeeBusiness;

    @Inject
    private DepartmentBusiness departmentBusiness;

    @Inject
    private FunctionBusiness functionBusiness;

    @Inject
    private EvaluationBusiness evaluationBusiness;

    @Inject
    private AuthBean authBean;

    private int employeeCount;
    private int departmentCount;
    private int functionCount;
    private int evaluationCount;

    /**
     * Loads dashboard counters when the hub is opened.
     */
    @PostConstruct
    public void init() {
        if (authBean.hasPermission("employee:read")) {
            loadEmployeeCount();
        }

        if (authBean.hasPermission("department:read")) {
            loadDepartmentCount();
        }

        if (authBean.hasPermission("function:read")) {
            loadFunctionCount();
        }

        if (authBean.hasPermission("evaluation:read")) {
            loadEvaluationCount();
        }
    }

    private void loadEmployeeCount() {
        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeList();

        if (result.isSuccess()) {
            employeeCount = result.getData().size();
        } else {
            log.warn("Unable to load employee count for hub");
        }
    }

    private void loadDepartmentCount() {
        Result<List<Department>> result = departmentBusiness.getAllDepartments();

        if (result.isSuccess()) {
            departmentCount = countActiveDepartments(result.getData());
        } else {
            log.warn("Unable to load department count for hub");
        }
    }

    private void loadFunctionCount() {
        Result<List<Function>> result = functionBusiness.getAllFunctions();

        if (result.isSuccess()) {
            functionCount = countActiveFunctions(result.getData());
        } else {
            log.warn("Unable to load function count for hub");
        }
    }

    private void loadEvaluationCount() {
        Result<List<Evaluation>> result = evaluationBusiness.getActiveEvaluations();

        if (result.isSuccess()) {
            evaluationCount = result.getData().size();
        } else {
            log.warn("Unable to load evaluation count for hub");
        }
    }

    private int countActiveDepartments(List<Department> departments) {
        int count = 0;

        for (Department department : departments) {
            if (Boolean.TRUE.equals(department.getIsActive())) {
                count++;
            }
        }

        return count;
    }

    private int countActiveFunctions(List<Function> functions) {
        int count = 0;

        for (Function function : functions) {
            if (Boolean.TRUE.equals(function.getIsActive())) {
                count++;
            }
        }

        return count;
    }

    public int getEmployeeCount() {
        return employeeCount;
    }

    public int getDepartmentCount() {
        return departmentCount;
    }

    public int getFunctionCount() {
        return functionCount;
    }

    public int getEvaluationCount() {
        return evaluationCount;
    }
}
