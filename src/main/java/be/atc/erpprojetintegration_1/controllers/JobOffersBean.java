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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean JSF utilisé par la page de gestion des offres d'emploi.
 * Il fournit la liste des offres et les actions utilisateur à la vue XHTML.
 */
@Named
@ViewScoped
public class JobOffersBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobOffersBean.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    @Inject
    private AuthBean authBean;

    private List<JobOffer> jobOffers;

    @PostConstruct
    public void init() {
        loadJobOffers();
    }

    /**
     * Gère le bouton de publication depuis la liste de gestion.
     */
    public void publish(Integer id) {
        if (!authBean.hasPermission("job-offer:publish")) {
            MessageUtils.addErrorMessage("jobOffers.error.access.denied");
            return;
        }

        Result<Void> result = jobOfferBusiness.publish(id);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.publish.error");
            return;
        }

        MessageUtils.addInfoMessage("jobOffers.publish.success");
        loadJobOffers();
    }

    /**
     * Gère le bouton d'archivage depuis la liste de gestion.
     */
    public void archive(Integer id) {
        if (!authBean.hasPermission("job-offer:archive")) {
            MessageUtils.addErrorMessage("jobOffers.error.access.denied");
            return;
        }

        Result<Void> result = jobOfferBusiness.archive(id);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "jobOffers.archive.error");
            return;
        }

        MessageUtils.addInfoMessage("jobOffers.archive.success");
        loadJobOffers();
    }

    /**
     * Gère le bouton de suppression logique depuis la liste de gestion.
     */
    public void softDelete(Integer id) {
        if (!authBean.hasPermission("job-offer:delete")) {
            MessageUtils.addErrorMessage("jobOffers.error.access.denied");
            return;
        }

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
     * Contrôle l'affichage du bouton de publication pour une ligne.
     */
    public boolean canPublish(JobOffer jobOffer) {
        return jobOffer != null
                && Boolean.TRUE.equals(jobOffer.getIsActive())
                && JobOfferStatus.NOT_PUBLISHED.equals(jobOffer.getStatus());
    }

    /**
     * Contrôle l'affichage du bouton d'archivage pour une ligne.
     */
    public boolean canArchive(JobOffer jobOffer) {
        return jobOffer != null
                && Boolean.TRUE.equals(jobOffer.getIsActive())
                && JobOfferStatus.PUBLISHED.equals(jobOffer.getStatus());
    }

    /**
     * Contrôle l'affichage du bouton de suppression logique pour une ligne.
     */
    public boolean canDelete(JobOffer jobOffer) {
        return jobOffer != null && Boolean.TRUE.equals(jobOffer.getIsActive());
    }

    /**
     * Formate une date pour l'affichage dans le tableau de gestion.
     *
     * @param date date à formater
     * @return date formatée
     */
    public String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    /**
     * Formate une date et une heure pour l'affichage dans le tableau de gestion.
     *
     * @param dateTime date et heure à formater
     * @return date et heure formatées
     */
    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? "" : dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Retourne le libelle traduit d'un statut d'offre.
     *
     * @param status statut de l'offre
     * @return libelle traduit
     */
    public String getStatusLabel(JobOfferStatus status) {
        return status == null ? "" : MessageUtils.getMessage(getStatusMessageKey(status));
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

    public List<JobOffer> getJobOffers() {
        return jobOffers;
    }
}
