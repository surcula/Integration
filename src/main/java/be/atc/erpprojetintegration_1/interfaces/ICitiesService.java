package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface ICitiesService {

    /**
     * Retrieves all cities.
     *
     * @return city list result
     */
    Result<List<City>> getAll();

    /**
     * Retrieves all active cities.
     *
     * @return active city list result
     */
    Result<List<City>> getAllActiveCities();

    /**
     * Retrieves active cities by zip code.
     *
     * @param zip city zip code
     * @return active city list result
     */
    Result<List<City>> getActiveByZip(int zip);

    /**
     * Retrieves a city by id.
     *
     * @param id city id
     * @return city result
     */
    Result<City> getById(Integer id);

    /**
     * Creates a city.
     *
     * @param city city to create
     * @return created city result
     */
    Result<City> create(City city);

    /**
     * Updates a city.
     *
     * @param city city to update
     * @return updated city result
     */
    Result<City> update(City city);

    /**
     * Updates city active status.
     *
     * @param id city id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);

}
