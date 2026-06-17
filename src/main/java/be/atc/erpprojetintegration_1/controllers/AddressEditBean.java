package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.AddressBusiness;
import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class AddressEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(AddressEditBean.class);

    @Inject
    private AddressBusiness addressBusiness;

    @Inject
    private AuthBean authBean;

    private Integer addressId;
    private Address address;
    private List<City> cities;
    private City selectedCity;

    public void loadAddress() {
        loadCities();

        if (addressId == null) {
            address = new Address();
            address.setIsActive(true);
            selectedCity = null;
            return;
        }

        Result<Address> result = addressBusiness.getAddressById(addressId);

        if (result.isSuccess()) {
            address = result.getData();
            selectedCity = address.getCity();
            log.info("Address edit page loaded for id: " + addressId);
        } else {
            address = null;
            MessageUtils.addErrorMessages(result, "addresses.error.load");
        }
    }

    public String save() {
        if (isCreateMode() && !authBean.hasPermission("address:create")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return null;
        }

        if (!isCreateMode() && !authBean.hasPermission("address:edit")) {
            MessageUtils.addErrorMessage("common.accessDenied");
            return null;
        }

        address.setCity(selectedCity);
        Result<Address> result = addressBusiness.saveAddress(address);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, "addresses.error.save");
            return null;
        }

        MessageUtils.addInfoMessage(addressId == null ? "addresses.create.success" : "addresses.edit.success");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/addresses?faces-redirect=true";
    }

    public List<City> completeCity(String query) {
        String search = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);

        return cities.stream()
                .filter(city -> city.getCityName().toLowerCase(Locale.ROOT).contains(search)
                        || city.getZipCode().toString().contains(search))
                .limit(20)
                .collect(Collectors.toList());
    }

    private void loadCities() {
        Result<List<City>> result = addressBusiness.getActiveCities();

        if (result.isSuccess()) {
            cities = result.getData();
        } else {
            cities = new ArrayList<>();
            MessageUtils.addErrorMessages(result, "addresses.error.cities.load");
        }
    }

    public boolean isCreateMode() {
        return addressId == null;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public Address getAddress() {
        return address;
    }

    public City getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;
    }
}
