package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Superior;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines employee superior database operations.
 */
public interface ISuperiorService {

    /**
     * Retrieves all employee superior assignments.
     *
     * @return superior assignment list result
     */
    Result<List<Superior>> getAll();

    Result<List<Superior>> getActiveBySuperiorId(Integer superiorId);

    /**
     * Retrieves a superior assignment by id.
     *
     * @param id superior assignment id
     * @return superior assignment result
     */
    Result<Superior> getById(Integer id);

    /**
     * Creates a superior assignment.
     *
     * @param superior superior assignment to create
     * @return created superior assignment result
     */
    Result<Superior> create(Superior superior);

    /**
     * Updates a superior assignment.
     *
     * @param superior superior assignment to update
     * @return updated superior assignment result
     */
    Result<Superior> update(Superior superior);

    /**
     * Updates superior assignment active status.
     *
     * @param id superior assignment id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
