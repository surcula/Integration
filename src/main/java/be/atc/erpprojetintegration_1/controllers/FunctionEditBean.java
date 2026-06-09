package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.FunctionBusiness;
import be.atc.erpprojetintegration_1.entities.Function;
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
public class FunctionEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(FunctionEditBean.class);

    @Inject
    private FunctionBusiness functionBusiness;

    private Integer functionId;
    private Function function;

    public void loadFunction() {
        if (functionId == null) {
            function = new Function();
            function.setIsActive(true);
            function.setMandatory(false);
            return;
        }

        Result<Function> result = functionBusiness.getFunctionById(functionId);

        if (result.isSuccess()) {
            function = result.getData();
            log.info("Function edit page loaded for id: " + functionId);
        } else {
            function = null;
            MessageUtils.addErrorMessages(result, "functions.error.load");
        }
    }

    public String save() {
        Result<Function> result = functionBusiness.saveFunction(function);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "functions.error.save");
            return null;
        }

        MessageUtils.addInfoMessage(functionId == null ? "functions.create.success" : "functions.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/functions?faces-redirect=true";
    }

    public boolean isCreateMode() {
        return functionId == null;
    }

    public Integer getFunctionId() {
        return functionId;
    }

    public void setFunctionId(Integer functionId) {
        this.functionId = functionId;
    }

    public Function getFunction() {
        return function;
    }
}
