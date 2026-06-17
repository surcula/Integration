package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.JobOfferBusiness;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Bean JSF utilisé par la page publique de détail d'une offre.
 * Il charge uniquement une offre publiée et active.
 */
@Named
@ViewScoped
public class JobOfferDetailBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobOfferDetailBean.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    private Integer jobOfferId;
    private JobOffer jobOffer;

    /**
     * Charge l'offre publiée sélectionnée pour la consultation.
     */
    public void loadJobOffer() {
        Result<JobOffer> result = jobOfferBusiness.getPublishedActiveJobOfferById(jobOfferId);

        if (result.isSuccess()) {
            jobOffer = result.getData();
            log.info("Published job offer detail loaded for id: " + jobOfferId);
        } else {
            jobOffer = null;
            MessageUtils.addErrorMessages(result, "jobOffers.error.load");
        }
    }

    /**
     * Retourne l'identifiant reçu depuis l'URL de la page.
     *
     * @return identifiant de l'offre
     */
    public Integer getJobOfferId() {
        return jobOfferId;
    }

    /**
     * Stocke l'identifiant reçu depuis l'URL de la page.
     *
     * @param jobOfferId identifiant de l'offre
     */
    public void setJobOfferId(Integer jobOfferId) {
        this.jobOfferId = jobOfferId;
    }

    /**
     * Retourne l'offre publiée et active affichée sur la page de détail.
     *
     * @return offre sélectionnée
     */
    public JobOffer getJobOffer() {
        return jobOffer;
    }

    /**
     * Formate une date pour la page de détail.
     *
     * @param date date à formater
     * @return date formatée
     */
    public String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }
}
