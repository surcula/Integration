package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines function-related database operations.
 */
public interface IFunctionService {

    /**
     * Retrieves all functions.
     *
     * @return function list result
     */
    Result<List<Function>> getAll();

    /**
     * Retrieves a function by id.
     *
     * @param id function id
     * @return function result
     */
    Result<Function> getById(Integer id);

    /**
     * Creates a function.
     *
     * @param function function to create
     * @return created function result
     */
    Result<Function> create(Function function);

    /**
     * Updates a function.
     *
     * @param function function to update
     * @return updated function result
     */
    Result<Function> update(Function function);

    /**
     * Updates function active status.
     *
     * @param id function id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
