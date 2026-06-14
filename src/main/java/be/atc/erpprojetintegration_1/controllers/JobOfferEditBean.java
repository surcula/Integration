package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.FunctionBusiness;
import be.atc.erpprojetintegration_1.business.JobOfferBusiness;
import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * JSF bean used by the job offer create and edit page.
 * It loads the selected offer, the available functions and saves the submitted form.
 */
@Named
@ViewScoped
public class JobOfferEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobOfferEditBean.class);

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    @Inject
    private FunctionBusiness functionBusiness;

    private Integer jobOfferId;
    private Integer selectedFunctionId;
    private JobOffer jobOffer;
    private List<Function> functions;

    /**
     * Loads the offer to edit, or prepares an empty offer when the page is in create mode.
     */
    public void loadJobOffer() {
        loadFunctions();

        if (jobOfferId == null) {
            jobOffer = new JobOffer();
            return;
        }

        Result<JobOffer> result = jobOfferBusiness.getJobOfferById(jobOfferId);

        if (result.isSuccess()) {
            jobOffer = result.getData();

            if (jobOffer.getFunction() != null) {
                selectedFunctionId = jobOffer.getFunction().getId();
            }

            log.info("Job offer edit page loaded for id: " + jobOfferId);
        } else {
            jobOffer = null;
            MessageUtils.addErrorMessages(result, "jobOffers.error.load");
        }
    }

    /**
     * Saves the form and redirects to the list page when the operation succeeds.
     */
    public String save() {
        Result<JobOffer> result = jobOfferBusiness.saveJobOffer(jobOffer, selectedFunctionId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.error.save");
            return null;
        }

        MessageUtils.addInfoMessage(jobOfferId == null ? "jobOffers.create.success" : "jobOffers.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/job-offers?faces-redirect=true";
    }

    private void loadFunctions() {
        Result<List<Function>> result = functionBusiness.getAllFunctions();

        if (result.isSuccess()) {
            functions = result.getData();
        } else {
            functions = new ArrayList<>();
            MessageUtils.addErrorMessages(result, "functions.error.load");
        }
    }

    public boolean isCreateMode() {
        return jobOfferId == null;
    }

    public Integer getJobOfferId() {
        return jobOfferId;
    }

    public void setJobOfferId(Integer jobOfferId) {
        this.jobOfferId = jobOfferId;
    }

    public Integer getSelectedFunctionId() {
        return selectedFunctionId;
    }

    public void setSelectedFunctionId(Integer selectedFunctionId) {
        this.selectedFunctionId = selectedFunctionId;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public List<Function> getFunctions() {
        return functions;
    }
}
