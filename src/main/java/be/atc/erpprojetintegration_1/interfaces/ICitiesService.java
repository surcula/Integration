package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface ICitiesService {


        /**
         * Retrieves Active Cities
         * @return all the active cities
         */
        Result<List<City>> getAllActiveCities();

        /**
         * Retrieves Cities by zipcode
         * @param zip zipcode of the cities
         * @return all the zipcode of the cities
         */
        Result<List<City>> getActiveByZip(int zip);



}
