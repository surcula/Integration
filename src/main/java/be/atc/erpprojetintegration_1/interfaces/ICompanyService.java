package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Company;
import be.atc.erpprojetintegration_1.tools.Result;

/**
 * Defines company-related database operations.
 */
public interface ICompanyService {

    /**
     * Retrieves the active company.
     *
     * @return active company result
     */
    Result<Company> getActiveCompany();

    /**
     * Updates company information.
     *
     * @param company company to update
     * @return updated company result
     */
    Result<Company> update(Company company);
}
