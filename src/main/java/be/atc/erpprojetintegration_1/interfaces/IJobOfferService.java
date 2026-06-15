package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Contrat de service pour les opérations de persistance des offres d'emploi.
 * Les classes Business utilisent cette interface au lieu de dépendre directement de l'implémentation.
 */
public interface IJobOfferService {

    /**
     * Récupère toutes les offres pour la gestion.
     *
     * @return résultat contenant toutes les offres
     */
    Result<List<JobOffer>> getAll();

    /**
     * Récupère toutes les offres actives.
     *
     * @return résultat contenant les offres actives
     */
    Result<List<JobOffer>> getAllActive();

    /**
     * Récupère les offres actives et publiées liées à une fonction.
     *
     * @param functionId identifiant de la fonction
     * @return résultat contenant les offres correspondantes
     */
    Result<List<JobOffer>> getActiveByFunctionId(Integer functionId);

    /**
     * Récupère les offres publiées et actives pour la consultation publique.
     *
     * @return résultat contenant les offres publiées et actives
     */
    Result<List<JobOffer>> getPublishedActive();

    /**
     * Récupère une offre à partir de son identifiant.
     *
     * @param id identifiant de l'offre
     * @return résultat contenant l'offre
     */
    Result<JobOffer> getById(Integer id);

    /**
     * Récupère une offre publiée et active à partir de son identifiant.
     *
     * @param id identifiant de l'offre
     * @return résultat contenant l'offre publiée et active
     */
    Result<JobOffer> getPublishedActiveById(Integer id);

    /**
     * Enregistre une nouvelle offre.
     *
     * @param jobOffer offre à créer
     * @return résultat contenant l'offre créée
     */
    Result<JobOffer> create(JobOffer jobOffer);

    /**
     * Modifie une offre existante.
     *
     * @param jobOffer offre à modifier
     * @return résultat contenant l'offre modifiée
     */
    Result<JobOffer> update(JobOffer jobOffer);

    /**
     * Publie une offre.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    Result<Void> publish(Integer id);

    /**
     * Archive une offre.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    Result<Void> archive(Integer id);

    /**
     * Supprime logiquement une offre.
     *
     * @param id identifiant de l'offre
     * @return résultat de l'opération
     */
    Result<Void> softDelete(Integer id);
}
