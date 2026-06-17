package be.atc.erpprojetintegration_1.services;

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

@ApplicationScoped
public class EvaluationServiceImpl implements IEvaluationService {

    private static final Logger log = Logger.getLogger(EvaluationServiceImpl.class);

    @Override
    public Result<List<Evaluation>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active evaluations");

            List<Evaluation> evaluations = em
                    .createNamedQuery("getAllActiveEvaluations", Evaluation.class)
                    .getResultList();

            log.info("Active evaluations found: " + evaluations.size());
            return Result.ok(evaluations);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "evaluations.error.load");

            log.error("Error while searching all active evaluations", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
