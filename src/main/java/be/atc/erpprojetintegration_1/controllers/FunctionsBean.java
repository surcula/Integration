package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.FunctionBusiness;
import be.atc.erpprojetintegration_1.entities.Function;
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
public class FunctionsBean implements Serializable {

    private static final Logger log = Logger.getLogger(FunctionsBean.class);

    @Inject
    private FunctionBusiness functionBusiness;

    private List<Function> functions;

    @PostConstruct
    public void init() {
        loadFunctions();
    }

    public void changeFunctionActiveStatus(Integer id, boolean active) {
        Result<Void> result = functionBusiness.setActive(id, !active);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, active ? "functions.delete.error" : "functions.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(active ? "functions.delete.success" : "functions.activate.success");
        loadFunctions();
    }

    private void loadFunctions() {
        Result<List<Function>> result = functionBusiness.getAllFunctions();

        if (result.isSuccess()) {
            functions = result.getData();
            log.info("Functions loaded in list page: " + functions.size());
        } else {
            functions = new ArrayList<>();
            log.warn("Function list loading failed");
            MessageUtils.addErrorMessages(result, "functions.error.load");
        }
    }

    public List<Function> getFunctions() {
        return functions;
    }
}
