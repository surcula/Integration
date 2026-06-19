package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.interfaces.IEvaluationService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation JPA du service des evaluations.
 * Cette classe gere uniquement les acces a la base de donnees.
 */
@ApplicationScoped
public class EvaluationServiceImpl implements IEvaluationService {

    private static final Logger log = Logger.getLogger(EvaluationServiceImpl.class);

    /**
     * Recupere toutes les evaluations actives avec employe et evaluateur.
     *
     * @return resultat contenant les evaluations actives
     */
    @Override
    public Result<List<Evaluation>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            List<Evaluation> evaluations = em
                    .createNamedQuery("getAllActiveEvaluations", Evaluation.class)
                    .getResultList();
            return Result.ok(evaluations);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "evaluations.error.load");
            log.error("Error while loading evaluations", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Recupere une evaluation par son identifiant.
     *
     * @param id identifiant de l'evaluation
     * @return resultat contenant l'evaluation
     */
    @Override
    public Result<Evaluation> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            Evaluation evaluation = em
                    .createNamedQuery("getEvaluationById", Evaluation.class)
                    .setParameter("id", id)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (evaluation == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "evaluations.error.notFound");
                return Result.fail(errors);
            }

            return Result.ok(evaluation);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "evaluations.error.load");
            log.error("Error while loading evaluation id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Cree une evaluation.
     *
     * @param evaluation evaluation a creer
     * @return resultat contenant l'evaluation creee
     */
    @Override
    public Result<Evaluation> create(Evaluation evaluation) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            attachEmployees(em, evaluation);
            em.persist(evaluation);
            em.getTransaction().commit();
            return Result.ok(evaluation);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "evaluations.error.save");
            log.error("Error while creating evaluation", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Modifie une evaluation existante.
     *
     * @param evaluation evaluation a modifier
     * @return resultat contenant l'evaluation modifiee
     */
    @Override
    public Result<Evaluation> update(Evaluation evaluation) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            attachEmployees(em, evaluation);
            Evaluation updatedEvaluation = em.merge(evaluation);
            em.getTransaction().commit();
            return Result.ok(updatedEvaluation);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "evaluations.error.save");
            log.error("Error while updating evaluation", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * Supprime logiquement une evaluation.
     *
     * @param id identifiant de l'evaluation
     * @return resultat de l'operation
     */
    @Override
    public Result<Void> softDelete(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            em.getTransaction().begin();
            Evaluation evaluation = em.find(Evaluation.class, id);

            if (evaluation == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "evaluations.error.notFound");
                return Result.fail(errors);
            }

            evaluation.setIsActive(false);
            em.merge(evaluation);
            em.getTransaction().commit();
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "evaluations.error.delete");
            log.error("Error while deleting evaluation id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    private void attachEmployees(EntityManager em, Evaluation evaluation) {
        Employee employee = em.find(Employee.class, evaluation.getEmployee().getId());
        Employee evaluator = em.find(Employee.class, evaluation.getEvaluator().getId());
        evaluation.setEmployee(employee);
        evaluation.setEvaluator(evaluator);
    }
}
