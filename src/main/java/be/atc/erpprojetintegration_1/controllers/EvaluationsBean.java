package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EvaluationBusiness;
import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.enums.EvaluationStatus;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean JSF de la page liste des evaluations.
 * Il prepare les statistiques, la recherche et les actions de gestion.
 */
@Named
@ViewScoped
public class EvaluationsBean implements Serializable {

    private static final Logger log = Logger.getLogger(EvaluationsBean.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Inject
    private EvaluationBusiness evaluationBusiness;

    @Inject
    private AuthBean authBean;

    private List<Evaluation> evaluations;
    private List<Evaluation> filteredEvaluations;
    private String keyword;
    private List<EvaluationStatus> selectedStatuses = new ArrayList<>();

    /**
     * Initialise la liste des evaluations au chargement de la page.
     */
    @PostConstruct
    public void init() {
        loadEvaluations();
    }

    /**
     * Applique la recherche globale sur les evaluations visibles.
     */
    public void filterEvaluations() {
        if (evaluations == null) {
            filteredEvaluations = new ArrayList<>();
            return;
        }

        String search = keyword == null ? "" : keyword.trim().toLowerCase();

        filteredEvaluations = evaluations.stream()
                .filter(evaluation -> selectedStatuses.isEmpty()
                        || selectedStatuses.contains(evaluation.getStatus()))
                .filter(evaluation -> search.isEmpty() || containsKeyword(evaluation, search))
                .collect(Collectors.toList());
    }

    /**
     * Ajoute ou retire un statut du filtre de la liste.
     *
     * @param status statut selectionne
     */
    public void toggleStatusFilter(EvaluationStatus status) {
        if (selectedStatuses.contains(status)) {
            selectedStatuses.remove(status);
        } else {
            selectedStatuses.add(status);
        }

        filterEvaluations();
    }

    /**
     * Reinitialise le filtre de statut.
     */
    public void clearStatusFilter() {
        selectedStatuses.clear();
        filterEvaluations();
    }

    /**
     * Supprime logiquement une evaluation.
     *
     * @param id identifiant de l'evaluation
     */
    public void softDelete(Integer id) {
        if (!authBean.hasPermission("evaluation:delete")) {
            MessageUtils.addErrorMessage("evaluations.error.access.denied");
            return;
        }

        Result<Void> result = evaluationBusiness.softDelete(id, authBean.getConnectedEmployee());

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "evaluations.delete.error");
            return;
        }

        MessageUtils.addInfoMessage("evaluations.delete.success");
        log.info("Evaluation supprimee logiquement avec id: " + id);
        loadEvaluations();
    }

    /**
     * Compte les evaluations d'un statut.
     *
     * @param status statut a compter
     * @return nombre d'evaluations
     */
    public long countByStatus(EvaluationStatus status) {
        if (evaluations == null) {
            return 0;
        }

        return evaluations.stream()
                .filter(evaluation -> status.equals(evaluation.getStatus()))
                .count();
    }

    /**
     * Formate une date pour l'affichage.
     *
     * @param date date a formater
     * @return date formatee
     */
    public String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DATE_FORMATTER);
    }

    /**
     * Formate la periode de l'evaluation.
     *
     * @param evaluation evaluation concernee
     * @return periode formatee
     */
    public String formatPeriod(Evaluation evaluation) {
        if (evaluation == null || (evaluation.getPeriodStart() == null && evaluation.getPeriodEnd() == null)) {
            return "-";
        }

        return formatDate(evaluation.getPeriodStart()) + " - " + formatDate(evaluation.getPeriodEnd());
    }

    /**
     * Formate le score global.
     *
     * @param score score global
     * @return score formate
     */
    public String formatScore(BigDecimal score) {
        return score == null ? "-" : score.stripTrailingZeros().toPlainString();
    }

    /**
     * Affiche un apercu court du commentaire dans le tableau.
     *
     * @param comments commentaire complet
     * @return commentaire raccourci ou tiret si vide
     */
    public String formatCommentPreview(String comments) {
        if (comments == null || comments.trim().isEmpty()) {
            return "-";
        }

        String cleanedComments = comments.trim();
        return cleanedComments.length() <= 60 ? cleanedComments : cleanedComments.substring(0, 60) + "...";
    }

    /**
     * Indique si le bouton de creation peut etre affiche.
     *
     * @return true si la creation est autorisee
     */
    public boolean isCreateAllowed() {
        return authBean.hasPermission("evaluation:create")
                && evaluationBusiness.canCreate(authBean.getConnectedEmployee());
    }

    /**
     * Indique si une evaluation peut etre modifiee.
     *
     * @param evaluation evaluation concernee
     * @return true si la modification est autorisee
     */
    public boolean canEdit(Evaluation evaluation) {
        return authBean.hasPermission("evaluation:edit")
                && evaluationBusiness.canEdit(evaluation, authBean.getConnectedEmployee());
    }

    /**
     * Indique si une evaluation peut etre supprimee logiquement.
     *
     * @param evaluation evaluation concernee
     * @return true si la suppression logique est autorisee
     */
    public boolean canDelete(Evaluation evaluation) {
        return authBean.hasPermission("evaluation:delete")
                && evaluationBusiness.canEdit(evaluation, authBean.getConnectedEmployee());
    }

    /**
     * Retourne le libelle traduit d'un statut.
     *
     * @param status statut
     * @return libelle traduit
     */
    public String getStatusLabel(EvaluationStatus status) {
        return status == null ? "" : MessageUtils.getMessage("evaluations.status." + status.getCode());
    }

    /**
     * Retourne la cle i18n d'un statut.
     *
     * @param status statut
     * @return cle du fichier messages
     */
    public String getStatusMessageKey(EvaluationStatus status) {
        return status == null ? "" : "evaluations.status." + status.getCode();
    }

    /**
     * Retourne les classes CSS du badge de statut.
     *
     * @param status statut
     * @return classes CSS
     */
    public String getStatusBadgeClass(EvaluationStatus status) {
        if (EvaluationStatus.IN_PROGRESS.equals(status)) {
            return "evaluation-status-badge evaluation-status-in-progress";
        }

        if (EvaluationStatus.COMPLETED.equals(status)) {
            return "evaluation-status-badge evaluation-status-completed";
        }

        if (EvaluationStatus.SCORE_CALCULATED.equals(status)) {
            return "evaluation-status-badge evaluation-status-score";
        }

        if (EvaluationStatus.VALIDATED.equals(status)) {
            return "evaluation-status-badge evaluation-status-validated";
        }

        return "evaluation-status-badge evaluation-status-created";
    }

    /**
     * Indique si une carte de statut est selectionnee.
     *
     * @param status statut a verifier
     * @return true si le statut est selectionne
     */
    public boolean isStatusSelected(EvaluationStatus status) {
        return selectedStatuses.contains(status);
    }

    /**
     * Retourne les classes CSS d'une carte de statut.
     *
     * @param status statut represente par la carte
     * @return classes CSS
     */
    public String getStatusCardClass(EvaluationStatus status) {
        String cssClass = "evaluation-stat-card ";

        if (EvaluationStatus.IN_PROGRESS.equals(status)) {
            cssClass += "evaluation-card-progress";
        } else if (EvaluationStatus.COMPLETED.equals(status)) {
            cssClass += "evaluation-card-completed";
        } else if (EvaluationStatus.SCORE_CALCULATED.equals(status)) {
            cssClass += "evaluation-card-score";
        } else if (EvaluationStatus.VALIDATED.equals(status)) {
            cssClass += "evaluation-card-validated";
        } else {
            cssClass += "evaluation-card-created";
        }

        return selectedStatuses.contains(status) ? cssClass + " evaluation-card-selected" : cssClass;
    }

    /**
     * Retourne l'icone PrimeIcons associee a un statut.
     *
     * @param status statut
     * @return classe d'icone
     */
    public String getStatusIconClass(EvaluationStatus status) {
        if (EvaluationStatus.IN_PROGRESS.equals(status)) {
            return "pi pi-clock";
        }

        if (EvaluationStatus.COMPLETED.equals(status)) {
            return "pi pi-check-square";
        }

        if (EvaluationStatus.SCORE_CALCULATED.equals(status)) {
            return "pi pi-chart-bar";
        }

        if (EvaluationStatus.VALIDATED.equals(status)) {
            return "pi pi-check-circle";
        }

        return "pi pi-file";
    }

    private void loadEvaluations() {
        Result<List<Evaluation>> result = evaluationBusiness.getVisibleEvaluations(authBean.getConnectedEmployee());

        if (result.isSuccess()) {
            evaluations = result.getData();
            filteredEvaluations = new ArrayList<>(evaluations);
            log.info("Evaluations loaded: " + evaluations.size());
        } else {
            evaluations = new ArrayList<>();
            filteredEvaluations = new ArrayList<>();
            MessageUtils.addErrorMessages(result, "evaluations.error.load");
        }
    }

    private boolean containsKeyword(Evaluation evaluation, String search) {
        return contains(evaluation.getEvaluationName(), search)
                || contains(getEmployeeFullName(evaluation), search)
                || contains(getEvaluatorFullName(evaluation), search)
                || contains(getStatusLabel(evaluation.getStatus()), search)
                || contains(formatDate(evaluation.getEvaluationDate()), search)
                || contains(formatPeriod(evaluation), search)
                || contains(evaluation.getComments(), search)
                || contains(formatScore(evaluation.getGlobalScore()), search);
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    public String getEmployeeFullName(Evaluation evaluation) {
        if (evaluation == null || evaluation.getEmployee() == null) {
            return "";
        }

        return (evaluation.getEmployee().getLastName() + " " + evaluation.getEmployee().getFirstName()).trim();
    }

    public String getEvaluatorFullName(Evaluation evaluation) {
        if (evaluation == null || evaluation.getEvaluator() == null) {
            return "";
        }

        return (evaluation.getEvaluator().getLastName() + " " + evaluation.getEvaluator().getFirstName()).trim();
    }

    public List<Evaluation> getFilteredEvaluations() {
        return filteredEvaluations;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public EvaluationStatus getCreatedStatus() {
        return EvaluationStatus.CREATED;
    }

    public EvaluationStatus getInProgressStatus() {
        return EvaluationStatus.IN_PROGRESS;
    }

    public EvaluationStatus getCompletedStatus() {
        return EvaluationStatus.COMPLETED;
    }

    public EvaluationStatus getScoreCalculatedStatus() {
        return EvaluationStatus.SCORE_CALCULATED;
    }

    public EvaluationStatus getValidatedStatus() {
        return EvaluationStatus.VALIDATED;
    }
}
