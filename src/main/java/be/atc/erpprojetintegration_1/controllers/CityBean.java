package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.interfaces.ICitiesService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Named
@RequestScoped
public class CityBean {
    @Inject
    private ICitiesService cityService;

    private Result<List<City>> cities;

    /**
     * Loads active cities when the page is initialized.
     */
    @PostConstruct
    public void init() {
        cities = cityService.getAllActiveCities();
    }

    /**
     * Returns active cities for the JSF page.
     *
     * @return active city list
     */
    public List<City> getCities() {
        if (cities == null || !cities.isSuccess() || cities.getData() == null) {
            return new ArrayList<>();
        }
        return cities.getData();
    }
}
