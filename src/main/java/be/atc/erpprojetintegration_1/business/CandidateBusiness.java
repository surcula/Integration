package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Candidate;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.entities.JobOffersCandidate;
import be.atc.erpprojetintegration_1.enums.CandidateApplicationStatus;
import be.atc.erpprojetintegration_1.enums.JobOfferStatus;
import be.atc.erpprojetintegration_1.interfaces.ICandidateService;
import be.atc.erpprojetintegration_1.interfaces.IJobOfferService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Couche Business des candidatures.
 * Elle centralise les validations et les règles métier avant l'appel au service.
 */
@ApplicationScoped
public class CandidateBusiness {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Inject
    private ICandidateService candidateService;

    @Inject
    private IJobOfferService jobOfferService;

    /**
     * Récupère toutes les candidatures pour la page de suivi.
     *
     * @return résultat contenant les candidatures
     */
    public Result<List<JobOffersCandidate>> getAllApplications() {
        return candidateService.getAllApplications();
    }

    /**
     * Récupère une candidature à modifier.
     *
     * @param id identifiant de la candidature
     * @return résultat contenant la candidature
     */
    public Result<JobOffersCandidate> getApplicationById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "candidates.error.id.required");
            return Result.fail(errors);
        }

        return candidateService.getApplicationById(id);
    }

    /**
     * Crée ou modifie une candidature après validation.
     *
     * @param application candidature saisie dans le formulaire
     * @param jobOfferId identifiant de l'offre sélectionnée
     * @return résultat contenant la candidature enregistrée
     */
    public Result<JobOffersCandidate> saveApplication(JobOffersCandidate application, Integer jobOfferId) {
        Result<Void> validationResult = validateApplication(application, jobOfferId);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Result<JobOffer> jobOfferResult = jobOfferService.getPublishedActiveById(jobOfferId);

        if (!jobOfferResult.isSuccess()) {
            Map<String, String> errors = new HashMap<>();
            errors.put("jobOffer", "candidates.error.jobOffer.notFound");
            return Result.fail(errors);
        }

        JobOffer jobOffer = jobOfferResult.getData();

        if (!JobOfferStatus.PUBLISHED.equals(jobOffer.getStatus()) || !Boolean.TRUE.equals(jobOffer.getIsActive())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("jobOffer", "candidates.error.jobOffer.notAvailable");
            return Result.fail(errors);
        }

        Result<Void> duplicateResult = validateDuplicate(application, jobOfferId);

        if (!duplicateResult.isSuccess()) {
            return Result.fail(duplicateResult.getErrors());
        }

        trimApplicationFields(application);
        application.setJobOffers(jobOffer);
        application.setJobOfferName(jobOffer.getJobOfferName());

        if (application.getApplicationDate() == null) {
            application.setApplicationDate(LocalDate.now());
        }

        if (application.getApplicationStatus() == null) {
            application.setApplicationStatus(CandidateApplicationStatus.RECEIVED);
        }

        if (application.getId() == null) {
            application.setIsActive(true);
            application.getCandidate().setIsActive(true);
            return candidateService.createApplication(application);
        }

        return candidateService.updateApplication(application);
    }

    /**
     * Supprime logiquement une candidature.
     *
     * @param id identifiant de la candidature
     * @return résultat de l'opération
     */
    public Result<Void> softDeleteApplication(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "candidates.error.id.required");
            return Result.fail(errors);
        }

        return candidateService.softDeleteApplication(id);
    }

    private Result<Void> validateApplication(JobOffersCandidate application, Integer jobOfferId) {
        Map<String, String> errors = new HashMap<>();

        if (application == null || application.getCandidate() == null) {
            errors.put("application", "candidates.error.form.invalid");
            return Result.fail(errors);
        }

        Candidate candidate = application.getCandidate();
        FormValidator.required(candidate.getFirstName(), "firstName", "candidates.error.firstName.required", errors);
        FormValidator.required(candidate.getLastName(), "lastName", "candidates.error.lastName.required", errors);
        FormValidator.required(candidate.getEmail(), "email", "candidates.error.email.required", errors);
        FormValidator.lengthBetween(candidate.getFirstName(), "firstName", "candidates.error.firstName.length", 1, 100, errors);
        FormValidator.lengthBetween(candidate.getLastName(), "lastName", "candidates.error.lastName.length", 1, 100, errors);
        FormValidator.lengthBetween(candidate.getEmail(), "email", "candidates.error.email.length", 1, 150, errors);
        FormValidator.lengthBetween(candidate.getPhone(), "phone", "candidates.error.phone.length", 0, 20, errors);

        if (candidate.getEmail() != null && !candidate.getEmail().trim().isEmpty()
                && !EMAIL_PATTERN.matcher(candidate.getEmail().trim()).matches()) {
            errors.put("email", "candidates.error.email.invalid");
        }

        validateBirthDate(candidate.getBirthDate(), errors);

        if (jobOfferId == null) {
            errors.put("jobOffer", "candidates.error.jobOffer.required");
        }

        if (application.getApplicationStatus() == null) {
            errors.put("applicationStatus", "candidates.error.status.required");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void validateBirthDate(LocalDate birthDate, Map<String, String> errors) {
        if (birthDate == null) {
            errors.put("birthDate", "candidates.error.birthDate.required");
            return;
        }

        if (birthDate.isAfter(LocalDate.now())) {
            errors.put("birthDate", "candidates.error.birthDate.future");
            return;
        }

        if (birthDate.isAfter(LocalDate.now().minusYears(15))) {
            errors.put("birthDate", "candidates.error.birthDate.minimumAge");
        }
    }

    private Result<Void> validateDuplicate(JobOffersCandidate application, Integer jobOfferId) {
        Result<JobOffersCandidate> existingResult = candidateService
                .getActiveApplicationByEmailAndJobOffer(application.getCandidate().getEmail(), jobOfferId);

        if (!existingResult.isSuccess()) {
            return Result.fail(existingResult.getErrors());
        }

        JobOffersCandidate existingApplication = existingResult.getData();

        if (existingApplication != null && !existingApplication.getId().equals(application.getId())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("email", "candidates.error.duplicate");
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void trimApplicationFields(JobOffersCandidate application) {
        Candidate candidate = application.getCandidate();
        candidate.setFirstName(trim(candidate.getFirstName()));
        candidate.setLastName(trim(candidate.getLastName()));
        candidate.setEmail(trim(candidate.getEmail()));
        candidate.setPhone(trim(candidate.getPhone()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
