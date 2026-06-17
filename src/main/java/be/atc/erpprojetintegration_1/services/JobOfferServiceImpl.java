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
 * Implémentation JPA du service des offres d'emploi.
 * Elle gère uniquement l'accès à la base et utilise les NamedQuery définies dans JobOffer.
 */
@ApplicationScoped
public class JobOfferServiceImpl implements IJobOfferService {

    private static final Logger log = Logger.getLogger(JobOfferServiceImpl.class);

    /**
     * Récupère toutes les offres avec leur fonction pour le tableau de gestion.
     *
     * @return résultat contenant toutes les offres
     */
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

    /**
     * Récupère toutes les offres actives.
     *
     * @return résultat contenant les offres actives
     */
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

    /**
     * Récupère les offres publiées et actives pour une fonction.
     *
     * @param functionId identifiant de la fonction
     * @return résultat contenant les offres correspondantes
     */
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

    /**
     * Récupère les offres visibles sur la vue publique.
     *
     * @return résultat contenant les offres publiées et actives
     */
    @Override
    public Result<List<JobOffer>> getPublishedActive() {
        EntityManager em = EMF.getEM();

        try {
            List<JobOffer> jobOffers = em.createNamedQuery("getPublishedActiveJobOffers", JobOffer.class)
                    .setParameter("status", JobOfferStatus.PUBLISHED)
                    .getResultList();
            return Result.ok(jobOffers);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "jobOffers.error.load");
            log.error("Error while searching published active job offers", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Récupère une offre par identifiant pour la gestion.
     *
     * @param id identifiant de l'offre
     * @return résultat contenant l'offre
     */
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

    /**
     * Récupère une offre publiée et active pour la page de détail.
     *
     * @param id identifiant de l'offre
     * @return résultat contenant l'offre publiée et active
     */
    @Override
    public Result<JobOffer> getPublishedActiveById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            JobOffer jobOffer = em.createNamedQuery("getPublishedActiveJobOfferById", JobOffer.class)
                    .setParameter("id", id)
                    .setParameter("status", JobOfferStatus.PUBLISHED)
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
            log.error("Error while searching published active job offer by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Crée une offre dans la base de données.
     *
     * @param jobOffer offre à enregistrer
     * @return résultat contenant l'offre créée
     */
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

    /**
     * Modifie une offre dans la base de données.
     *
     * @param jobOffer offre à modifier
     * @return résultat contenant l'offre modifiée
     */
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
     * Passe l'offre au statut PUBLISHED et initialise la date de publication si nécessaire.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    @Override
    public Result<Void> publish(Integer id) {
        return updateStatus(id, JobOfferStatus.PUBLISHED, true, true);
    }

    /**
     * Passe l'offre au statut ARCHIVED tout en la gardant active.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    @Override
    public Result<Void> archive(Integer id) {
        return updateStatus(id, JobOfferStatus.ARCHIVED, true, false);
    }

    /**
     * Marque l'offre comme supprimée en modifiant à la fois status et isActive.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    @Override
    public Result<Void> softDelete(Integer id) {
        return updateStatus(id, JobOfferStatus.DELETED, false, false);
    }

    /**
     * Méthode commune utilisée pour publier, archiver et supprimer logiquement une offre.
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

            // La première date de publication est conservée si l'offre est republiée plus tard.
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
