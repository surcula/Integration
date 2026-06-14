package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.enums.JobOfferStatus;
import be.atc.erpprojetintegration_1.interfaces.IJobOfferService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA implementation of the job offer service.
 * It only manages database access and keeps JPQL queries inside JobOffer named queries.
 */
@ApplicationScoped
public class JobOfferServiceImpl implements IJobOfferService {

    private static final Logger log = Logger.getLogger(JobOfferServiceImpl.class);

    @Override
    public Result<List<JobOffer>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            List<JobOffer> jobOffers = em.createNamedQuery("getAllJobOffers", JobOffer.class).getResultList();
            return Result.ok(jobOffers);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.load");
            log.error("Error while searching all job offers", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<JobOffer>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            List<JobOffer> jobOffers = em.createNamedQuery("getAllActiveJobOffers", JobOffer.class).getResultList();
            return Result.ok(jobOffers);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.load");
            log.error("Error while searching active job offers", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<JobOffer>> getActiveByFunctionId(Integer functionId) {
        EntityManager em = EMF.getEM();

        try {
            List<JobOffer> jobOffers = em.createNamedQuery("getActiveJobOffersByFunctionId", JobOffer.class)
                    .setParameter("functionId", functionId)
                    .getResultList();
            return Result.ok(jobOffers);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.load");
            log.error("Error while searching job offers by function id: " + functionId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<JobOffer> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            JobOffer jobOffer = em.createNamedQuery("getJobOfferById", JobOffer.class)
                    .setParameter("id", id)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (jobOffer == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "jobOffers.error.notFound");
                return Result.fail(errors);
            }

            return Result.ok(jobOffer);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.load");
            log.error("Error while searching job offer by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<JobOffer> create(JobOffer jobOffer) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            em.persist(jobOffer);
            em.getTransaction().commit();
            return Result.ok(jobOffer);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.save");
            log.error("Error while creating job offer", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<JobOffer> update(JobOffer jobOffer) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            JobOffer updatedJobOffer = em.merge(jobOffer);
            em.getTransaction().commit();
            return Result.ok(updatedJobOffer);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.save");
            log.error("Error while updating job offer id: " + jobOffer.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Changes the offer status to PUBLISHED and initializes the publication date if needed.
     */
    @Override
    public Result<Void> publish(Integer id) {
        return updateStatus(id, JobOfferStatus.PUBLISHED, true, true);
    }

    /**
     * Changes the offer status to ARCHIVED while keeping the offer active.
     */
    @Override
    public Result<Void> archive(Integer id) {
        return updateStatus(id, JobOfferStatus.ARCHIVED, true, false);
    }

    /**
     * Marks the offer as deleted by changing both status and isActive.
     */
    @Override
    public Result<Void> softDelete(Integer id) {
        return updateStatus(id, JobOfferStatus.DELETED, false, false);
    }

    /**
     * Shared persistence method used by publication, archive and logical deletion actions.
     */
    private Result<Void> updateStatus(Integer id, JobOfferStatus status, boolean active, boolean setPublishDate) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            JobOffer jobOffer = em.find(JobOffer.class, id);

            if (jobOffer == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "jobOffers.error.notFound");
                return Result.fail(errors);
            }

            jobOffer.setStatus(status);
            jobOffer.setIsActive(active);

            // The first publication date is kept if the offer is republished later.
            if (setPublishDate && jobOffer.getPublishStartDate() == null) {
                jobOffer.setPublishStartDate(LocalDate.now());
            }

            em.merge(jobOffer);
            em.getTransaction().commit();
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.status.update");
            log.error("Error while updating job offer status. Id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
