package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.CandidateBusiness;
import be.atc.erpprojetintegration_1.entities.JobOffersCandidate;
import be.atc.erpprojetintegration_1.enums.CandidateApplicationStatus;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean JSF utilisé par la page de suivi des candidatures.
 * Il prépare les statistiques, la recherche et les actions de gestion.
 */
@Named
@ViewScoped
public class CandidatesBean implements Serializable {

    private static final Logger log = Logger.getLogger(CandidatesBean.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    private CandidateBusiness candidateBusiness;

    private List<JobOffersCandidate> applications;
    private List<JobOffersCandidate> filteredApplications;
    private String keyword;
    private List<CandidateApplicationStatus> selectedStatuses = new ArrayList<>();

    /**
     * Initialise la liste des candidatures au chargement de la page.
     */
    @PostConstruct
    public void init() {
        loadApplications();
    }

    /**
     * Applique la recherche globale et le filtre par statut.
     */
    public void filterApplications() {
        if (applications == null) {
            filteredApplications = new ArrayList<>();
            return;
        }

        String search = keyword == null ? "" : keyword.trim().toLowerCase();

        filteredApplications = applications.stream()
                .filter(application -> selectedStatuses.isEmpty()
                        || selectedStatuses.contains(application.getApplicationStatus()))
                .filter(application -> search.isEmpty() || containsKeyword(application, search))
                .collect(Collectors.toList());

        sortApplicationsByNewest(filteredApplications);
    }

    /**
     * Filtre le tableau sur un statut depuis une carte statistique.
     *
     * @param status statut sélectionné
     */
    public void filterByStatus(CandidateApplicationStatus status) {
        toggleStatusFilter(status);
    }

    /**
     * Ajoute ou retire un statut du filtre depuis une carte statistique.
     *
     * @param status statut sélectionné
     */
    public void toggleStatusFilter(CandidateApplicationStatus status) {
        if (selectedStatuses.contains(status)) {
            selectedStatuses.remove(status);
        } else {
            selectedStatuses.add(status);
        }

        filterApplications();
    }

    /**
     * Réinitialise le filtre de statut et affiche toutes les candidatures.
     */
    public void clearStatusFilter() {
        selectedStatuses.clear();
        filterApplications();
    }

    /**
     * Supprime logiquement une candidature depuis le tableau de suivi.
     *
     * @param id identifiant de la candidature
     */
    public void softDelete(Integer id) {
        Result<Void> result = candidateBusiness.softDeleteApplication(id);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "candidates.delete.error");
            return;
        }

        MessageUtils.addInfoMessage("candidates.delete.success");
        loadApplications();
    }

    /**
     * Compte les candidatures correspondant à un statut.
     *
     * @param status statut à compter
     * @return nombre de candidatures
     */
    public long countByStatus(CandidateApplicationStatus status) {
        if (applications == null) {
            return 0;
        }

        return applications.stream()
                .filter(application -> status.equals(application.getApplicationStatus()))
                .count();
    }

    /**
     * Retourne le nombre total de candidatures chargées.
     *
     * @return nombre total de candidatures
     */
    public long getApplicationsCount() {
        return applications == null ? 0 : applications.size();
    }

    /**
     * Formate une date pour l'affichage.
     *
     * @param date date à formater
     * @return date formatée
     */
    public String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private void loadApplications() {
        Result<List<JobOffersCandidate>> result = candidateBusiness.getAllApplications();

        if (result.isSuccess()) {
            applications = result.getData();
            sortApplicationsByNewest(applications);
            filteredApplications = new ArrayList<>(applications);
            log.info("Applications loaded: " + applications.size());
        } else {
            applications = new ArrayList<>();
            filteredApplications = new ArrayList<>();
            MessageUtils.addErrorMessages(result, "candidates.error.load");
        }
    }

    private boolean containsKeyword(JobOffersCandidate application, String search) {
        return contains(application.getCandidate().getFirstName(), search)
                || contains(application.getCandidate().getLastName(), search)
                || contains(application.getCandidate().getEmail(), search)
                || contains(application.getCandidate().getPhone(), search)
                || contains(application.getJobOfferName(), search)
                || (application.getJobOffers() != null && contains(application.getJobOffers().getJobOfferName(), search))
                || (application.getApplicationStatus() != null && contains(application.getApplicationStatus().getLabel(), search));
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    /**
     * Trie les candidatures pour afficher la dernière candidature créée en premier.
     *
     * @param applicationsToSort liste à trier
     */
    private void sortApplicationsByNewest(List<JobOffersCandidate> applicationsToSort) {
        if (applicationsToSort == null) {
            return;
        }

        applicationsToSort.sort((first, second) -> {
            int idComparison = compareIdsDesc(first.getId(), second.getId());

            if (idComparison != 0) {
                return idComparison;
            }

            return compareDatesDesc(first.getApplicationDate(), second.getApplicationDate());
        });
    }

    private int compareDatesDesc(LocalDate firstDate, LocalDate secondDate) {
        if (firstDate == null && secondDate == null) {
            return 0;
        }

        if (firstDate == null) {
            return 1;
        }

        if (secondDate == null) {
            return -1;
        }

        return secondDate.compareTo(firstDate);
    }

    private int compareIdsDesc(Integer firstId, Integer secondId) {
        if (firstId == null && secondId == null) {
            return 0;
        }

        if (firstId == null) {
            return 1;
        }

        if (secondId == null) {
            return -1;
        }

        return secondId.compareTo(firstId);
    }

    public List<JobOffersCandidate> getFilteredApplications() {
        return filteredApplications;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public CandidateApplicationStatus[] getApplicationStatuses() {
        return CandidateApplicationStatus.values();
    }

    public String getStatusLabel(CandidateApplicationStatus status) {
        return status == null ? "" : MessageUtils.getMessage("candidates.status." + status.getCode());
    }

    /**
     * Indique si la carte "Tous" doit être affichée comme sélectionnée.
     *
     * @return true si aucun filtre de statut n'est actif
     */
    public boolean isAllStatusesSelected() {
        return selectedStatuses.isEmpty();
    }

    /**
     * Indique si une carte de statut doit être affichée comme sélectionnée.
     *
     * @param status statut à vérifier
     * @return true si le statut est sélectionné
     */
    public boolean isStatusSelected(CandidateApplicationStatus status) {
        return selectedStatuses.contains(status);
    }

    /**
     * Retourne les classes CSS du badge affiché dans le tableau.
     *
     * @param status statut de la candidature
     * @return classes CSS du badge
     */
    public String getStatusBadgeClass(CandidateApplicationStatus status) {
        if (CandidateApplicationStatus.UNDER_REVIEW.equals(status)) {
            return "candidate-status-badge candidate-status-review status-review";
        }

        if (CandidateApplicationStatus.INTERVIEW.equals(status)) {
            return "candidate-status-badge candidate-status-interview status-interview";
        }

        if (CandidateApplicationStatus.ACCEPTED.equals(status)) {
            return "candidate-status-badge candidate-status-accepted status-accepted";
        }

        if (CandidateApplicationStatus.REJECTED.equals(status)) {
            return "candidate-status-badge candidate-status-rejected status-rejected";
        }

        if (CandidateApplicationStatus.ARCHIVED.equals(status)) {
            return "candidate-status-badge candidate-status-archived status-archived";
        }

        return "candidate-status-badge status-received";
    }

    /**
     * Retourne les classes CSS d'une carte statistique de statut.
     *
     * @param status statut représenté par la carte
     * @return classes CSS de la carte
     */
    public String getStatusCardClass(CandidateApplicationStatus status) {
        String baseClass = "card h-100 shadow-sm candidate-stat-card text-decoration-none "
                + getStatusCardColorClass(status);

        if (selectedStatuses.contains(status)) {
            return baseClass + " candidate-stat-card-active candidate-stat-selected";
        }

        return baseClass;
    }

    /**
     * Retourne les classes CSS de la carte qui affiche toutes les candidatures.
     *
     * @return classes CSS de la carte "Tous"
     */
    public String getAllCardClass() {
        String baseClass = "card h-100 shadow-sm candidate-stat-card text-decoration-none candidate-stat-all stat-all";
        return selectedStatuses.isEmpty() ? baseClass + " candidate-stat-card-active candidate-stat-selected" : baseClass;
    }

    private String getStatusCardColorClass(CandidateApplicationStatus status) {
        if (CandidateApplicationStatus.UNDER_REVIEW.equals(status)) {
            return "candidate-stat-review stat-review";
        }

        if (CandidateApplicationStatus.INTERVIEW.equals(status)) {
            return "candidate-stat-interview stat-interview";
        }

        if (CandidateApplicationStatus.ACCEPTED.equals(status)) {
            return "candidate-stat-accepted stat-accepted";
        }

        if (CandidateApplicationStatus.REJECTED.equals(status)) {
            return "candidate-stat-rejected stat-rejected";
        }

        if (CandidateApplicationStatus.ARCHIVED.equals(status)) {
            return "candidate-stat-archived stat-archived";
        }

        return "candidate-stat-pending stat-received";
    }

    public CandidateApplicationStatus getReceivedStatus() {
        return CandidateApplicationStatus.RECEIVED;
    }

    public CandidateApplicationStatus getUnderReviewStatus() {
        return CandidateApplicationStatus.UNDER_REVIEW;
    }

    public CandidateApplicationStatus getInterviewStatus() {
        return CandidateApplicationStatus.INTERVIEW;
    }

    public CandidateApplicationStatus getAcceptedStatus() {
        return CandidateApplicationStatus.ACCEPTED;
    }

    public CandidateApplicationStatus getRejectedStatus() {
        return CandidateApplicationStatus.REJECTED;
    }

    public CandidateApplicationStatus getArchivedStatus() {
        return CandidateApplicationStatus.ARCHIVED;
    }
}
