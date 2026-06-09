package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.DepartmentBusiness;
import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class DepartmentEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(DepartmentEditBean.class);

    @Inject
    private DepartmentBusiness departmentBusiness;

    private Integer departmentId;
    private Department department;

    public void loadDepartment() {
        if (departmentId == null) {
            department = new Department();
            department.setIsActive(true);
            return;
        }

        Result<Department> result = departmentBusiness.getDepartmentById(departmentId);

        if (result.isSuccess()) {
            department = result.getData();
            log.info("Department edit page loaded for id: " + departmentId);
        } else {
            department = null;
            MessageUtils.addErrorMessages(result, "departments.error.load");
        }
    }

    public String save() {
        Result<Department> result = departmentBusiness.saveDepartment(department);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "departments.error.save");
            return null;
        }

        MessageUtils.addInfoMessage(departmentId == null ? "departments.create.success" : "departments.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/departments?faces-redirect=true";
    }

    public boolean isCreateMode() {
        return departmentId == null;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public Department getDepartment() {
        return department;
    }
}
