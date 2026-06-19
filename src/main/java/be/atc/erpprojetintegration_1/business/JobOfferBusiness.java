package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.enums.JobOfferStatus;
import be.atc.erpprojetintegration_1.interfaces.IFunctionService;
import be.atc.erpprojetintegration_1.interfaces.IJobOfferService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Couche Business des offres d'emploi.
 * Elle centralise les validations du formulaire et prépare l'entité avant l'appel au service.
 */
@ApplicationScoped
public class JobOfferBusiness {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    @Inject
    private IJobOfferService jobOfferService;

    @Inject
    private IFunctionService functionService;

    /**
     * Récupère toutes les offres pour la page de gestion.
     *
     * @return résultat contenant toutes les offres
     */
    public Result<List<JobOffer>> getAllJobOffers() {
        return jobOfferService.getAll();
    }

    /**
     * Récupère les offres actives et publiées pour une fonction donnée.
     *
     * @param functionId identifiant de la fonction
     * @return résultat contenant les offres correspondantes
     */
    public Result<List<JobOffer>> getActiveJobOffersByFunctionId(Integer functionId) {
        if (functionId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("functionId", "jobOffers.error.function.required");
            return Result.fail(errors);
        }

        return jobOfferService.getActiveByFunctionId(functionId);
    }

    /**
     * Récupère les offres visibles sur la page publique de consultation.
     *
     * @return résultat contenant les offres publiées et actives
     */
    public Result<List<JobOffer>> getPublishedActiveJobOffers() {
        return jobOfferService.getPublishedActive();
    }

    /**
     * Récupère une offre pour le formulaire de gestion.
     *
     * @param id identifiant de l'offre
     * @return résultat contenant l'offre
     */
    public Result<JobOffer> getJobOfferById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "jobOffers.error.id.required");
            return Result.fail(errors);
        }

        return jobOfferService.getById(id);
    }

    /**
     * Récupère une offre publiée et active pour la page publique de détail.
     *
     * @param id identifiant de l'offre
     * @return résultat contenant l'offre publiée et active
     */
    public Result<JobOffer> getPublishedActiveJobOfferById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "jobOffers.error.id.required");
            return Result.fail(errors);
        }

        return jobOfferService.getPublishedActiveById(id);
    }

    /**
     * Crée ou modifie une offre après validation des champs du formulaire et de la fonction sélectionnée.
     *
     * @param jobOffer données du formulaire de l'offre
     * @param functionId identifiant de la fonction sélectionnée
     * @return résultat contenant l'offre enregistrée
     */
    public Result<JobOffer> saveJobOffer(JobOffer jobOffer, Integer functionId) {
        Result<Void> validationResult = validateJobOffer(jobOffer, functionId);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Result<Function> functionResult = functionService.getById(functionId);

        if (!functionResult.isSuccess()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("function", "jobOffers.error.function.notFound");
            return Result.fail(errors);
        }

        trimJobOfferFields(jobOffer);
        jobOffer.setFunction(functionResult.getData());

        if (jobOffer.getId() == null) {
            // Une nouvelle offre est active ; NOT_PUBLISHED reste le statut par défaut.
            jobOffer.setCreateAt(LocalDateTime.now());
            if (jobOffer.getStatus() == null) {
                jobOffer.setStatus(JobOfferStatus.NOT_PUBLISHED);
            }
            jobOffer.setIsActive(true);
            return jobOfferService.create(jobOffer);
        }

        return jobOfferService.update(jobOffer);
    }

    /**
     * Publie une offre en déléguant le changement de statut à la couche Service.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    public Result<Void> publish(Integer id) {
        Result<Void> validationResult = validateId(id);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return jobOfferService.publish(id);
    }

    /**
     * Archive une offre publiée sans supprimer son historique.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    public Result<Void> archive(Integer id) {
        Result<Void> validationResult = validateId(id);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return jobOfferService.archive(id);
    }

    /**
     * Supprime logiquement une offre en conservant la ligne en base pour l'historique.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    public Result<Void> softDelete(Integer id) {
        Result<Void> validationResult = validateId(id);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return jobOfferService.softDelete(id);
    }

    /**
     * Valide les champs obligatoires selon le formulaire et le schéma de base de données.
     */
    private Result<Void> validateJobOffer(JobOffer jobOffer, Integer functionId) {
        Map<String, String> errors = new HashMap<>();

        if (jobOffer == null) {
            errors.put("jobOffer", "jobOffers.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(jobOffer.getJobOfferName(), "jobOfferName", "jobOffers.error.name.required", errors);
        FormValidator.lengthBetween(jobOffer.getJobOfferName(), "jobOfferName", "jobOffers.error.name.length", 1, 200, errors);
        FormValidator.lengthBetween(jobOffer.getEmail(), "email", "jobOffers.error.email.length", 0, 150, errors);
        FormValidator.lengthBetween(jobOffer.getContact(), "contact", "jobOffers.error.contact.length", 0, 150, errors);
        FormValidator.lengthBetween(jobOffer.getDuration(), "duration", "jobOffers.error.duration.length", 0, 100, errors);

        if (jobOffer.getEmail() != null && !jobOffer.getEmail().trim().isEmpty()
                && !EMAIL_PATTERN.matcher(jobOffer.getEmail().trim()).matches()) {
            errors.put("email", "jobOffers.error.email.invalid");
        }

        if (functionId == null) {
            errors.put("function", "jobOffers.error.function.required");
        }

        if (jobOffer.getStatus() == null) {
            errors.put("status", "jobOffers.error.status.required");
        }

        if (jobOffer.getNumberOfOpenPositions() != null && jobOffer.getNumberOfOpenPositions() <= 0) {
            errors.put("numberOfOpenPositions", "jobOffers.error.openPositions.positive");
        }

        LocalDate publishStartDate = jobOffer.getPublishStartDate();
        LocalDate publishEndDate = jobOffer.getPublishEndDate();

        if (publishStartDate != null && publishEndDate != null && publishEndDate.isBefore(publishStartDate)) {
            errors.put("publishEndDate", "jobOffers.error.publishEndDate.beforeStart");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private Result<Void> validateId(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "jobOffers.error.id.required");
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void trimJobOfferFields(JobOffer jobOffer) {
        jobOffer.setJobOfferName(trim(jobOffer.getJobOfferName()));
        jobOffer.setDescription(trim(jobOffer.getDescription()));
        jobOffer.setEmail(trim(jobOffer.getEmail()));
        jobOffer.setContact(trim(jobOffer.getContact()));
        jobOffer.setDuration(trim(jobOffer.getDuration()));
        jobOffer.setProfil(trim(jobOffer.getProfil()));
        jobOffer.setJobDescription(trim(jobOffer.getJobDescription()));
        jobOffer.setRequirements(trim(jobOffer.getRequirements()));
        jobOffer.setComments(trim(jobOffer.getComments()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
