package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Candidate;
import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.entities.JobOffersCandidate;
import be.atc.erpprojetintegration_1.enums.CandidateApplicationStatus;
import be.atc.erpprojetintegration_1.interfaces.ICandidateService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation JPA du service des candidatures.
 * Elle gère uniquement l'accès à la base de données.
 */
@ApplicationScoped
public class CandidateServiceImpl implements ICandidateService {

    private static final Logger log = Logger.getLogger(CandidateServiceImpl.class);

    /**
     * Récupère toutes les candidatures avec le candidat et l'offre associée.
     *
     * @return résultat contenant les candidatures
     */
    @Override
    public Result<List<JobOffersCandidate>> getAllApplications() {
        EntityManager em = EMF.getEM();

        try {
            List<JobOffersCandidate> applications = em
                    .createNamedQuery("getAllJobOfferCandidates", JobOffersCandidate.class)
                    .getResultList();
            return Result.ok(applications);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "candidates.error.load");
            log.error("Error while loading applications", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Récupère une candidature par identifiant.
     *
     * @param id identifiant de la candidature
     * @return résultat contenant la candidature
     */
    @Override
    public Result<JobOffersCandidate> getApplicationById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            JobOffersCandidate application = em
                    .createNamedQuery("getJobOfferCandidateById", JobOffersCandidate.class)
                    .setParameter("id", id)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (application == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "candidates.error.notFound");
                return Result.fail(errors);
            }

            return Result.ok(application);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "candidates.error.load");
            log.error("Error while loading application id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Recherche une candidature active déjà existante pour le même candidat et la même offre.
     *
     * @param email email du candidat
     * @param jobOfferId identifiant de l'offre
     * @return résultat contenant la candidature existante ou null
     */
    @Override
    public Result<JobOffersCandidate> getActiveApplicationByEmailAndJobOffer(String email, Integer jobOfferId) {
        EntityManager em = EMF.getEM();

        try {
            JobOffersCandidate application = em
                    .createNamedQuery("getActiveApplicationByCandidateEmailAndJobOffer", JobOffersCandidate.class)
                    .setParameter("email", email)
                    .setParameter("jobOfferId", jobOfferId)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);
            return Result.ok(application);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "candidates.error.load");
            log.error("Error while checking duplicate application", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Crée une candidature et persiste son candidat.
     *
     * @param application candidature à créer
     * @return résultat contenant la candidature créée
     */
    @Override
    public Result<JobOffersCandidate> createApplication(JobOffersCandidate application) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            JobOffer managedJobOffer = em.find(JobOffer.class, application.getJobOffers().getId());
            Candidate managedCandidate = prepareCandidate(em, application.getCandidate());

            application.setJobOffers(managedJobOffer);
            application.setCandidate(managedCandidate);
            em.persist(application);
            em.getTransaction().commit();
            return Result.ok(application);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "candidates.error.save");
            log.error("Error while creating application", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Modifie une candidature existante.
     *
     * @param application candidature à modifier
     * @return résultat contenant la candidature modifiée
     */
    @Override
    public Result<JobOffersCandidate> updateApplication(JobOffersCandidate application) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            JobOffer managedJobOffer = em.find(JobOffer.class, application.getJobOffers().getId());
            Candidate managedCandidate = prepareCandidate(em, application.getCandidate());

            application.setJobOffers(managedJobOffer);
            application.setCandidate(managedCandidate);
            JobOffersCandidate updatedApplication = em.merge(application);
            em.getTransaction().commit();
            return Result.ok(updatedApplication);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "candidates.error.save");
            log.error("Error while updating application", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Archive une candidature en la rendant inactive.
     *
     * @param id identifiant de la candidature
     * @return résultat de l'opération
     */
    @Override
    public Result<Void> softDeleteApplication(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            JobOffersCandidate application = em.find(JobOffersCandidate.class, id);

            if (application == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "candidates.error.notFound");
                return Result.fail(errors);
            }

            application.setIsActive(false);
            application.setApplicationStatus(CandidateApplicationStatus.ARCHIVED);
            em.merge(application);
            em.getTransaction().commit();
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "candidates.error.delete");
            log.error("Error while archiving application id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    private Candidate prepareCandidate(EntityManager em, Candidate candidate) {
        if (candidate.getId() == null) {
            Candidate existingCandidate = em
                    .createNamedQuery("getCandidateByEmail", Candidate.class)
                    .setParameter("email", candidate.getEmail())
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (existingCandidate != null) {
                existingCandidate.setFirstName(candidate.getFirstName());
                existingCandidate.setLastName(candidate.getLastName());
                existingCandidate.setBirthDate(candidate.getBirthDate());
                existingCandidate.setPhone(candidate.getPhone());
                existingCandidate.setIsActive(true);
                return em.merge(existingCandidate);
            }

            em.persist(candidate);
            return candidate;
        }

        return em.merge(candidate);
    }
}
