package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.CityBusiness;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class CitiesBean implements Serializable {

    private static final Logger log = Logger.getLogger(CitiesBean.class);

    @Inject
    private CityBusiness cityBusiness;

    private List<City> cities;

    @PostConstruct
    public void init() {
        loadCities();
    }

    public void changeCityActiveStatus(Integer id, boolean active) {
        Result<Void> result = cityBusiness.setActive(id, !active);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, active ? "cities.delete.error" : "cities.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(active ? "cities.delete.success" : "cities.activate.success");
        loadCities();
    }

    private void loadCities() {
        Result<List<City>> result = cityBusiness.getAllCities();

        if (result.isSuccess()) {
            cities = result.getData();
            log.info("Cities loaded in list page: " + cities.size());
        } else {
            cities = new ArrayList<>();
            log.warn("City list loading failed");
            MessageUtils.addErrorMessages(result, "cities.error.load");
        }
    }

    public List<City> getCities() {
        return cities;
    }
}
