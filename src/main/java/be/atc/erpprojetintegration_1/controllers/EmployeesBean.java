package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeDepartmentService;
import be.atc.erpprojetintegration_1.interfaces.IEmployeeService;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@RequestScoped
public class EmployeesBean implements Serializable {
    @Inject
    private EmployeeBusiness employeeBusiness;

    private List<EmployeeListDto> employees;

    @PostConstruct
    public void init() {
        Result<List<EmployeeListDto>> result = employeeBusiness.getEmployeeList();

        if (result.isSuccess()) {
            employees = result.getData();
        } else {
            MessageUtils.addErrorMessages(result, "employees.error.load");
        }
    }

    public List<EmployeeListDto> getEmployees() {
        return employees;
    }

}
