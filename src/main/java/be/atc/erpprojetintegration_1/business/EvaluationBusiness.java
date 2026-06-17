package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.interfaces.IEvaluationService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class EvaluationBusiness {

    @Inject
    private IEvaluationService evaluationService;

    /**
     * Retrieves active evaluations for the list page.
     *
     * @return active evaluation list result
     */
    public Result<List<Evaluation>> getActiveEvaluations() {
        return evaluationService.getAllActive();
    }
}
