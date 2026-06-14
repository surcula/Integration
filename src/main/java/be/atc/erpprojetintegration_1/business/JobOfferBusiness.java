package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.enums.JobOfferStatus;
import be.atc.erpprojetintegration_1.interfaces.IFunctionService;
import be.atc.erpprojetintegration_1.interfaces.IJobOfferService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business layer for job offers.
 * It centralizes form validation and prepares the entity before calling the service layer.
 */
@ApplicationScoped
public class JobOfferBusiness {

    @Inject
    private IJobOfferService jobOfferService;

    @Inject
    private IFunctionService functionService;

    public Result<List<JobOffer>> getAllJobOffers() {
        return jobOfferService.getAll();
    }

    public Result<List<JobOffer>> getActiveJobOffersByFunctionId(Integer functionId) {
        if (functionId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("functionId", "jobOffers.error.function.required");
            return Result.fail(errors);
        }

        return jobOfferService.getActiveByFunctionId(functionId);
    }

    public Result<JobOffer> getJobOfferById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "jobOffers.error.id.required");
            return Result.fail(errors);
        }

        return jobOfferService.getById(id);
    }

    /**
     * Creates or updates a job offer after validating the form fields and the selected function.
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
            // A newly created offer starts as active but not published.
            jobOffer.setCreateAt(LocalDateTime.now());
            jobOffer.setStatus(JobOfferStatus.NOT_PUBLISHED);
            jobOffer.setIsActive(true);
            return jobOfferService.create(jobOffer);
        }

        return jobOfferService.update(jobOffer);
    }

    /**
     * Publishes a job offer by delegating the status change to the service layer.
     */
    public Result<Void> publish(Integer id) {
        Result<Void> validationResult = validateId(id);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return jobOfferService.publish(id);
    }

    /**
     * Archives a published job offer without deleting its history.
     */
    public Result<Void> archive(Integer id) {
        Result<Void> validationResult = validateId(id);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return jobOfferService.archive(id);
    }

    /**
     * Logically deletes a job offer while keeping the database row for history.
     */
    public Result<Void> softDelete(Integer id) {
        Result<Void> validationResult = validateId(id);

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        return jobOfferService.softDelete(id);
    }

    /**
     * Validates the fields that are required by the form and the database schema.
     */
    private Result<Void> validateJobOffer(JobOffer jobOffer, Integer functionId) {
        Map<String, String> errors = new HashMap<>();

        if (jobOffer == null) {
            errors.put("jobOffer", "jobOffers.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(jobOffer.getJobOfferName(), "jobOfferName", "jobOffers.error.name.required", errors);
        FormValidator.lengthBetween(jobOffer.getJobOfferName(), "jobOfferName", "jobOffers.error.name.length", 1, 200, errors);

        if (functionId == null) {
            errors.put("function", "jobOffers.error.function.required");
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
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
