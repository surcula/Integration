package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.CityBusiness;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class CityEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(CityEditBean.class);

    @Inject
    private CityBusiness cityBusiness;

    @Inject
    private AuthBean authBean;

    private Integer cityId;
    private City city;

    public void loadCity() {
        if (cityId == null) {
            city = new City();
            city.setIsActive(true);
            return;
        }

        Result<City> result = cityBusiness.getCityById(cityId);

        if (result.isSuccess()) {
            city = result.getData();
            log.info("City edit page loaded for id: " + cityId);
        } else {
            city = null;
            MessageUtils.addErrorMessages(result, "cities.error.load");
        }
    }

    public String save() {
        if (isCreateMode() && !authBean.hasPermission("city:create")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return null;
        }

        if (!isCreateMode() && !authBean.hasPermission("city:edit")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return null;
        }

        Result<City> result = cityBusiness.saveCity(city);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "cities.error.save");
            return null;
        }

        MessageUtils.addInfoMessage(cityId == null ? "cities.create.success" : "cities.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/cities-admin?faces-redirect=true";
    }

    public boolean isCreateMode() {
        return cityId == null;
    }

    public Integer getCityId() {
        return cityId;
    }

    public void setCityId(Integer cityId) {
        this.cityId = cityId;
    }

    public City getCity() {
        return city;
    }
}
