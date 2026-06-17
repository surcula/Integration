package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.JobOffersCandidate;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Contrat de service pour les opérations de persistance des candidatures.
 */
public interface ICandidateService {

    /**
     * Récupère toutes les candidatures pour la page de suivi RH.
     *
     * @return résultat contenant les candidatures
     */
    Result<List<JobOffersCandidate>> getAllApplications();

    /**
     * Récupère une candidature à partir de son identifiant.
     *
     * @param id identifiant de la candidature
     * @return résultat contenant la candidature
     */
    Result<JobOffersCandidate> getApplicationById(Integer id);

    /**
     * Vérifie si une candidature active existe déjà pour un email et une offre.
     *
     * @param email email du candidat
     * @param jobOfferId identifiant de l'offre
     * @return résultat contenant la candidature existante ou null
     */
    Result<JobOffersCandidate> getActiveApplicationByEmailAndJobOffer(String email, Integer jobOfferId);

    /**
     * Crée une candidature et le candidat associé si nécessaire.
     *
     * @param application candidature à créer
     * @return résultat contenant la candidature créée
     */
    Result<JobOffersCandidate> createApplication(JobOffersCandidate application);

    /**
     * Modifie une candidature existante.
     *
     * @param application candidature à modifier
     * @return résultat contenant la candidature modifiée
     */
    Result<JobOffersCandidate> updateApplication(JobOffersCandidate application);

    /**
     * Supprime logiquement une candidature.
     *
     * @param id identifiant de la candidature
     * @return résultat de l'opération
     */
    Result<Void> softDeleteApplication(Integer id);
}
