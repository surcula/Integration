package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.JobOfferBusiness;
import be.atc.erpprojetintegration_1.entities.JobOffer;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bean JSF utilisé par la vue publique des offres d'emploi.
 * Il expose uniquement les offres publiées et actives pour la consultation.
 */
@Named
@ViewScoped
public class JobOffersPublicBean implements Serializable {

    private static final Logger log = Logger.getLogger(JobOffersPublicBean.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int OFFERS_STEP = 30;
    private static final String SORT_NEWEST = "NEWEST";
    private static final String SORT_OLDEST = "OLDEST";
    private static final String SORT_NAME_ASC = "NAME_ASC";
    private static final String SORT_NAME_DESC = "NAME_DESC";

    @Inject
    private JobOfferBusiness jobOfferBusiness;

    private List<JobOffer> jobOffers;
    private List<JobOffer> filteredJobOffers;
    private List<JobOffer> functionsFilter;
    private String keyword;
    private String sortOrder = SORT_NEWEST;
    private Integer selectedFunctionId;
    private int visibleOffersCount;

    /**
     * Initialise la vue publique au chargement de la page.
     */
    @PostConstruct
    public void init() {
        loadPublishedJobOffers();
    }

    /**
     * Charge uniquement les offres consultables par les employés.
     */
    public void loadPublishedJobOffers() {
        Result<List<JobOffer>> result = jobOfferBusiness.getPublishedActiveJobOffers();

        if (result.isSuccess()) {
            jobOffers = result.getData();
            sortPublicJobOffers(jobOffers);
            filteredJobOffers = new ArrayList<>(jobOffers);
            visibleOffersCount = OFFERS_STEP;
            buildFunctionsFilter();
            log.info("Published job offers loaded: " + jobOffers.size());
        } else {
            jobOffers = new ArrayList<>();
            filteredJobOffers = new ArrayList<>();
            functionsFilter = new ArrayList<>();
            visibleOffersCount = OFFERS_STEP;
            MessageUtils.addErrorMessages(result, "jobOffers.error.load");
        }
    }

    /**
     * Retourne une description raccourcie pour l'affichage dans une carte.
     *
     * @param description description complète de l'offre
     * @return description raccourcie
     */
    public String getShortDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "";
        }

        String cleanDescription = description.trim();

        if (cleanDescription.length() <= 160) {
            return cleanDescription;
        }

        return cleanDescription.substring(0, 160) + "...";
    }

    /**
     * Filtre la vue publique par mot-clé et par fonction.
     */
    public void filterJobOffers() {
        if (jobOffers == null) {
            filteredJobOffers = new ArrayList<>();
            return;
        }

        String search = keyword == null ? "" : keyword.trim().toLowerCase();

        filteredJobOffers = jobOffers.stream()
                .filter(offer -> selectedFunctionId == null || hasSelectedFunction(offer))
                .filter(offer -> search.isEmpty() || containsKeyword(offer, search))
                .collect(Collectors.toList());
        sortPublicJobOffers(filteredJobOffers);
        visibleOffersCount = OFFERS_STEP;
    }

    /**
     * Applique le tri choisi sur les offres déjà filtrées.
     */
    public void applySort() {
        if (filteredJobOffers != null) {
            sortPublicJobOffers(filteredJobOffers);
            visibleOffersCount = OFFERS_STEP;
        }
    }

    /**
     * Affiche 30 offres supplémentaires dans la vue publique.
     */
    public void showMoreOffers() {
        visibleOffersCount += OFFERS_STEP;
    }

    /**
     * Vérifie s'il reste des offres masquées après les cartes déjà visibles.
     *
     * @return true si d'autres offres peuvent être affichées
     */
    public boolean hasMoreOffers() {
        return filteredJobOffers != null && visibleOffersCount < filteredJobOffers.size();
    }

    /**
     * Formate une date de publication pour la vue publique.
     *
     * @param date date à formater
     * @return date formatée
     */
    public String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    private boolean containsKeyword(JobOffer offer, String search) {
        return contains(offer.getJobOfferName(), search)
                || contains(offer.getDescription(), search)
                || contains(offer.getProfil(), search)
                || contains(offer.getRequirements(), search)
                || (offer.getFunction() != null && contains(offer.getFunction().getFunctionName(), search));
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private boolean hasSelectedFunction(JobOffer offer) {
        return offer.getFunction() != null
                && offer.getFunction().getId() != null
                && selectedFunctionId.equals(offer.getFunction().getId());
    }

    private void sortPublicJobOffers(List<JobOffer> offers) {
        if (offers == null) {
            return;
        }

        if (SORT_OLDEST.equals(sortOrder)) {
            offers.sort((firstOffer, secondOffer) -> {
                int publishDateCompare = compareLocalDateAsc(firstOffer.getPublishStartDate(), secondOffer.getPublishStartDate());

                if (publishDateCompare != 0) {
                    return publishDateCompare;
                }

                return compareLocalDateTimeAsc(firstOffer.getCreateAt(), secondOffer.getCreateAt());
            });
            return;
        }

        if (SORT_NAME_ASC.equals(sortOrder)) {
            offers.sort((firstOffer, secondOffer) -> compareStringAsc(firstOffer.getJobOfferName(), secondOffer.getJobOfferName()));
            return;
        }

        if (SORT_NAME_DESC.equals(sortOrder)) {
            offers.sort((firstOffer, secondOffer) -> compareStringAsc(secondOffer.getJobOfferName(), firstOffer.getJobOfferName()));
            return;
        }

        offers.sort((firstOffer, secondOffer) -> {
            int publishDateCompare = compareLocalDateDesc(firstOffer.getPublishStartDate(), secondOffer.getPublishStartDate());

            if (publishDateCompare != 0) {
                return publishDateCompare;
            }

            return compareLocalDateTimeDesc(firstOffer.getCreateAt(), secondOffer.getCreateAt());
        });
    }

    private int compareLocalDateDesc(LocalDate firstDate, LocalDate secondDate) {
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

    private int compareLocalDateAsc(LocalDate firstDate, LocalDate secondDate) {
        if (firstDate == null && secondDate == null) {
            return 0;
        }

        if (firstDate == null) {
            return 1;
        }

        if (secondDate == null) {
            return -1;
        }

        return firstDate.compareTo(secondDate);
    }

    private int compareLocalDateTimeDesc(LocalDateTime firstDate, LocalDateTime secondDate) {
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

    private int compareLocalDateTimeAsc(LocalDateTime firstDate, LocalDateTime secondDate) {
        if (firstDate == null && secondDate == null) {
            return 0;
        }

        if (firstDate == null) {
            return 1;
        }

        if (secondDate == null) {
            return -1;
        }

        return firstDate.compareTo(secondDate);
    }

    private int compareStringAsc(String firstValue, String secondValue) {
        String firstText = firstValue == null ? "" : firstValue.toLowerCase();
        String secondText = secondValue == null ? "" : secondValue.toLowerCase();
        return firstText.compareTo(secondText);
    }

    private void buildFunctionsFilter() {
        Map<Integer, JobOffer> offersByFunction = new LinkedHashMap<>();

        for (JobOffer offer : jobOffers) {
            if (offer.getFunction() != null && offer.getFunction().getId() != null) {
                offersByFunction.putIfAbsent(offer.getFunction().getId(), offer);
            }
        }

        functionsFilter = new ArrayList<>(offersByFunction.values());
        functionsFilter.sort(Comparator.comparing(offer -> offer.getFunction().getFunctionName() == null
                ? ""
                : offer.getFunction().getFunctionName().toLowerCase()));
    }

    /**
     * Retourne les offres disponibles dans la vue publique.
     *
     * @return offres publiées et actives
     */
    public List<JobOffer> getJobOffers() {
        return jobOffers;
    }

    public List<JobOffer> getFilteredJobOffers() {
        return filteredJobOffers;
    }

    /**
     * Retourne le nombre d'offres après application de la recherche et du filtre fonction.
     *
     * @return nombre d'offres trouvées
     */
    public int getFilteredJobOffersCount() {
        return filteredJobOffers == null ? 0 : filteredJobOffers.size();
    }

    /**
     * Retourne uniquement les offres actuellement visibles dans la vue publique.
     *
     * @return offres publiées et actives visibles
     */
    public List<JobOffer> getVisibleJobOffers() {
        if (filteredJobOffers == null || filteredJobOffers.isEmpty()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(visibleOffersCount, filteredJobOffers.size());
        return filteredJobOffers.subList(0, endIndex);
    }

    public List<JobOffer> getFunctionsFilter() {
        return functionsFilter;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getSelectedFunctionId() {
        return selectedFunctionId;
    }

    public void setSelectedFunctionId(Integer selectedFunctionId) {
        this.selectedFunctionId = selectedFunctionId;
    }
}
