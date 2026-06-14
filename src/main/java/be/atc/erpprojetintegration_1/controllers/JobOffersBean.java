package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.JobOfferBusiness;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.enums.JobOfferStatus;
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

/**
 * JSF bean used by the job offer list page.
 * It exposes list data and user actions to the XHTML view.
 */
@Named
@ViewScoped
public class JobOffersBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobOffersBean.class);

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    private List<JobOffer> jobOffers;

    @PostConstruct
    public void init() {
        loadJobOffers();
    }

    /**
     * Handles the publish button from the list page.
     */
    public void publish(Integer id) {
        Result<Void> result = jobOfferBusiness.publish(id);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.publish.error");
            return;
        }

        MessageUtils.addInfoMessage("jobOffers.publish.success");
        loadJobOffers();
    }

    /**
     * Handles the archive button from the list page.
     */
    public void archive(Integer id) {
        Result<Void> result = jobOfferBusiness.archive(id);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.archive.error");
            return;
        }

        MessageUtils.addInfoMessage("jobOffers.archive.success");
        loadJobOffers();
    }

    /**
     * Handles the logical delete button from the list page.
     */
    public void softDelete(Integer id) {
        Result<Void> result = jobOfferBusiness.softDelete(id);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.delete.error");
            return;
        }

        MessageUtils.addInfoMessage("jobOffers.delete.success");
        loadJobOffers();
    }

    private void loadJobOffers() {
        Result<List<JobOffer>> result = jobOfferBusiness.getAllJobOffers();

        if (result.isSuccess()) {
            jobOffers = result.getData();
            log.info("Job offers loaded in list page: " + jobOffers.size());
        } else {
            jobOffers = new ArrayList<>();
            MessageUtils.addErrorMessages(result, "jobOffers.error.load");
        }
    }

    /**
     * Controls whether the publish button is displayed for one row.
     */
    public boolean canPublish(JobOffer jobOffer) {
        return jobOffer != null
                && Boolean.TRUE.equals(jobOffer.getIsActive())
                && JobOfferStatus.NOT_PUBLISHED.equals(jobOffer.getStatus());
    }

    /**
     * Controls whether the archive button is displayed for one row.
     */
    public boolean canArchive(JobOffer jobOffer) {
        return jobOffer != null
                && Boolean.TRUE.equals(jobOffer.getIsActive())
                && JobOfferStatus.PUBLISHED.equals(jobOffer.getStatus());
    }

    /**
     * Controls whether the logical delete button is displayed for one row.
     */
    public boolean canDelete(JobOffer jobOffer) {
        return jobOffer != null && Boolean.TRUE.equals(jobOffer.getIsActive());
    }

    public List<JobOffer> getJobOffers() {
        return jobOffers;
    }
}
