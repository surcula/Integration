package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.JobOffer;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Service contract for job offer persistence operations.
 * Business classes use this interface instead of depending directly on the implementation.
 */
public interface IJobOfferService {

    Result<List<JobOffer>> getAll();

    Result<List<JobOffer>> getAllActive();

    Result<List<JobOffer>> getActiveByFunctionId(Integer functionId);

    Result<JobOffer> getById(Integer id);

    Result<JobOffer> create(JobOffer jobOffer);

    Result<JobOffer> update(JobOffer jobOffer);

    Result<Void> publish(Integer id);

    Result<Void> archive(Integer id);

    Result<Void> softDelete(Integer id);
}
