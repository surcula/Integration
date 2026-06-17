package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines evaluation-related database operations.
 */
public interface IEvaluationService {

    /**
     * Retrieves active evaluations.
     *
     * @return active evaluation list result
     */
    Result<List<Evaluation>> getAllActive();
}
