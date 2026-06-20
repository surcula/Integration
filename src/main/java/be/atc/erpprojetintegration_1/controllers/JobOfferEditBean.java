package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.FunctionBusiness;
import be.atc.erpprojetintegration_1.business.JobOfferBusiness;
import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.enums.JobOfferStatus;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Bean JSF utilisé par la page de création et de modification d'une offre.
 * Il charge l'offre sélectionnée, les fonctions disponibles et enregistre le formulaire.
 */
@Named
@ViewScoped
public class JobOfferEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobOfferEditBean.class);

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    @Inject
    private FunctionBusiness functionBusiness;

    @Inject
    private AuthBean authBean;

    private Integer jobOfferId;
    private Integer selectedFunctionId;
    private JobOffer jobOffer;
    private List<Function> functions;

    /**
     * Charge l'offre à modifier ou prépare une nouvelle offre en mode création.
     */
    public void loadJobOffer() {
        loadFunctions();

        if (jobOfferId == null) {
            if (!authBean.hasPermission("job-offer:create")) {
                jobOffer = null;
                MessageUtils.addErrorMessage("jobOffers.error.access.denied");
                return;
            }

            jobOffer = new JobOffer();
            jobOffer.setStatus(JobOfferStatus.NOT_PUBLISHED);
            return;
        }

        if (!authBean.hasPermission("job-offer:edit")) {
            jobOffer = null;
            MessageUtils.addErrorMessage("jobOffers.error.access.denied");
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
     * Enregistre le formulaire et redirige vers la liste quand l'opération réussit.
     */
    public String save() {
        boolean createMode = jobOffer != null && jobOffer.getId() == null;

        if (createMode && !authBean.hasPermission("job-offer:create")) {
            MessageUtils.addErrorMessage("jobOffers.error.access.denied");
            return null;
        }

        if (!createMode && !authBean.hasPermission("job-offer:edit")) {
            MessageUtils.addErrorMessage("jobOffers.error.access.denied");
            return null;
        }

        Result<JobOffer> result = jobOfferBusiness.saveJobOffer(jobOffer, selectedFunctionId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.error.save");
            return null;
        }

        log.info((createMode ? "Offre creee" : "Offre modifiee")
                + " avec id: " + result.getData().getId());
        MessageUtils.addInfoMessage(createMode ? "jobOffers.create.success" : "jobOffers.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/job-offers?faces-redirect=true";
    }

    private void loadFunctions() {
        Result<List<Function>> result = functionBusiness.getAllFunctions();

        if (result.isSuccess()) {
            functions = result.getData();
            functions.sort(Comparator.comparing(function -> function.getFunctionName() == null ? "" : function.getFunctionName().toLowerCase()));
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

    /**
     * Retourne les statuts disponibles pour la liste déroulante du formulaire.
     *
     * @return statuts des offres d'emploi
     */
    public JobOfferStatus[] getJobOfferStatuses() {
        return JobOfferStatus.values();
    }

    /**
     * Retourne la cle i18n d'un statut d'offre.
     *
     * @param status statut de l'offre
     * @return cle du fichier messages
     */
    public String getStatusMessageKey(JobOfferStatus status) {
        return status == null ? "" : "jobOffers.status." + status.getCode();
    }
}
