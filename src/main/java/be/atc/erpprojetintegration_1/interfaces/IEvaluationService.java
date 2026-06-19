package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Evaluation;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Contrat de service pour les operations de persistance des evaluations.
 */
public interface IEvaluationService {

    /**
     * Recupere toutes les evaluations actives.
     *
     * @return resultat contenant les evaluations actives
     */
    Result<List<Evaluation>> getAllActive();

    /**
     * Recupere une evaluation par son identifiant.
     *
     * @param id identifiant de l'evaluation
     * @return resultat contenant l'evaluation
     */
    Result<Evaluation> getById(Integer id);

    /**
     * Cree une evaluation.
     *
     * @param evaluation evaluation a creer
     * @return resultat contenant l'evaluation creee
     */
    Result<Evaluation> create(Evaluation evaluation);

    /**
     * Modifie une evaluation existante.
     *
     * @param evaluation evaluation a modifier
     * @return resultat contenant l'evaluation modifiee
     */
    Result<Evaluation> update(Evaluation evaluation);

    /**
     * Supprime logiquement une evaluation.
     *
     * @param id identifiant de l'evaluation
     * @return resultat de l'operation
     */
    Result<Void> softDelete(Integer id);
}
