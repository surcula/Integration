package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Contract;
import be.atc.erpprojetintegration_1.tools.Result;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Defines contract-related database operations.
 */
public interface IContractService {

    /**
     * Retrieves all contracts, including closed contracts.
     *
     * @return contract list result
     */
    Result<List<Contract>> getAll();

    /**
     * Retrieves all contracts linked to one employee.
     *
     * @param employeeId employee id
     * @return employee contract list result
     */
    Result<List<Contract>> getByEmployee(Integer employeeId);

    /**
     * Retrieves the active contract of an employee.
     *
     * @param employeeId employee id
     * @return active contract result, or empty data if none exists
     */
    Result<Contract> getActive(Integer employeeId);

    /**
     * Retrieves a contract by its identifier.
     *
     * @param contractId contract id
     * @return contract result, or empty data if not found
     */
    Result<Contract> getById(Integer contractId);

    /**
     * Creates a contract for an employee.
     *
     * @param contract   contract to create
     * @param employeeId employee id
     * @return created contract result
     */
    Result<Contract> create(Contract contract, Integer employeeId);

    /**
     * Closes an active contract with an end date.
     *
     * @param contractId contract id
     * @param endDate    closing date
     * @return operation result
     */
    Result<Void> close(Integer contractId, LocalDate endDate);

    /**
     * Renews an active non-permanent contract by changing its end date.
     *
     * @param contractId contract id
     * @param newEndDate new contract end date
     * @return operation result
     */
    Result<Void> renew(Integer contractId, LocalDate newEndDate);

    /**
     * Updates the stored gross salary of an active contract.
     *
     * @param contractId contract id
     * @param newSalary  new gross salary
     * @return operation result
     */
    Result<Void> updateSalary(Integer contractId, BigDecimal newSalary);
}
