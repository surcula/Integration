package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface ICitiesService {

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

}
