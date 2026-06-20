package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.CandidateBusiness;
import be.atc.erpprojetintegration_1.business.JobOfferBusiness;
import be.atc.erpprojetintegration_1.entities.Candidate;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.entities.JobOffersCandidate;
import be.atc.erpprojetintegration_1.enums.CandidateApplicationStatus;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean JSF utilisé par le formulaire de création et de modification d'une candidature.
 */
@Named
@ViewScoped
public class CandidateEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(CandidateEditBean.class);

    @Inject
    private CandidateBusiness candidateBusiness;

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    @Inject
    private AuthBean authBean;

    private Integer applicationId;
    private Integer selectedJobOfferId;
    private JobOffersCandidate application;
    private List<JobOffer> publishedJobOffers;

    /**
     * Charge la candidature à modifier ou prépare une nouvelle candidature.
     */
    public void loadApplication() {
        loadPublishedJobOffers();

        if (applicationId == null) {
            if (!authBean.hasPermission("candidate:create")) {
                application = null;
                MessageUtils.addErrorMessage("candidates.access.denied");
                return;
            }

            application = new JobOffersCandidate();
            application.setCandidate(new Candidate());
            application.setApplicationStatus(CandidateApplicationStatus.RECEIVED);
            application.setIsActive(true);
            return;
        }

        if (!authBean.hasPermission("candidate:edit")) {
            application = null;
            MessageUtils.addErrorMessage("candidates.access.denied");
            return;
        }

        Result<JobOffersCandidate> result = candidateBusiness.getApplicationById(applicationId);

        if (result.isSuccess()) {
            application = result.getData();

            if (application.getJobOffers() != null) {
                selectedJobOfferId = application.getJobOffers().getId();
            }
        } else {
            application = null;
            MessageUtils.addErrorMessages(result, "candidates.error.load");
        }
    }

    /**
     * Enregistre le formulaire et retourne vers le suivi des candidatures.
     *
     * @return navigation JSF
     */
    public String save() {
        boolean createMode = application != null && application.getId() == null;

        if (createMode && !authBean.hasPermission("candidate:create")) {
            MessageUtils.addErrorMessage("candidates.access.denied");
            return null;
        }

        if (!createMode && !authBean.hasPermission("candidate:edit")) {
            MessageUtils.addErrorMessage("candidates.access.denied");
            return null;
        }

        Result<JobOffersCandidate> result = candidateBusiness.saveApplication(application, selectedJobOfferId);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "candidates.error.save");
            return null;
        }

        log.info((createMode ? "Candidature creee" : "Candidature modifiee")
                + " avec id: " + result.getData().getId());
        MessageUtils.addInfoMessage(createMode ? "candidates.create.success" : "candidates.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/applications?faces-redirect=true";
    }

    private void loadPublishedJobOffers() {
        Result<List<JobOffer>> result = jobOfferBusiness.getPublishedActiveJobOffers();

        if (result.isSuccess()) {
            publishedJobOffers = result.getData();
        } else {
            publishedJobOffers = new ArrayList<>();
            MessageUtils.addErrorMessages(result, "jobOffers.error.load");
        }
    }

    public boolean isCreateMode() {
        return applicationId == null;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getSelectedJobOfferId() {
        return selectedJobOfferId;
    }

    public void setSelectedJobOfferId(Integer selectedJobOfferId) {
        this.selectedJobOfferId = selectedJobOfferId;
    }

    public JobOffersCandidate getApplication() {
        return application;
    }

    public List<JobOffer> getPublishedJobOffers() {
        return publishedJobOffers;
    }

    public CandidateApplicationStatus[] getApplicationStatuses() {
        return CandidateApplicationStatus.values();
    }

    /**
     * Retourne la date de naissance maximale autorisée pour avoir au moins 15 ans.
     *
     * @return date maximale autorisée
     */
    public LocalDate getMaxBirthDate() {
        return LocalDate.now().minusYears(15);
    }

    /**
     * Retourne l'année maximale autorisée dans le calendrier de naissance.
     *
     * @return année maximale autorisée
     */
    public int getMaxBirthYear() {
        return getMaxBirthDate().getYear();
    }

    public String getStatusLabel(CandidateApplicationStatus status) {
        return status == null ? "" : MessageUtils.getMessage("candidates.status." + status.getCode());
    }

    /**
     * Retourne la cle i18n d'un statut de candidature.
     *
     * @param status statut de candidature
     * @return cle du fichier messages
     */
    public String getStatusMessageKey(CandidateApplicationStatus status) {
        return status == null ? "" : "candidates.status." + status.getCode();
    }
}
