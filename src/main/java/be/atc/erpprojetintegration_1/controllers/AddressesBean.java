package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.AddressBusiness;
import be.atc.erpprojetintegration_1.entities.Address;
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
import java.util.Locale;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class AddressesBean implements Serializable {

    private static final Logger log = Logger.getLogger(AddressesBean.class);

    @Inject
    private AddressBusiness addressBusiness;

    private List<Address> addresses;
    private String searchTerm;

    @PostConstruct
    public void init() {
        loadAddresses();
    }

    public void changeAddressActiveStatus(Integer id, boolean active) {
        Result<Void> result = addressBusiness.setActive(id, !active);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, active ? "addresses.delete.error" : "addresses.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(active ? "addresses.delete.success" : "addresses.activate.success");
        loadAddresses();
    }

    private void loadAddresses() {
        Result<List<Address>> result = addressBusiness.getAllAddresses();

        if (result.isSuccess()) {
            addresses = result.getData();
            log.info("Addresses loaded in list page: " + addresses.size());
        } else {
            addresses = new ArrayList<>();
            log.warn("Address list loading failed");
            MessageUtils.addErrorMessages(result, "addresses.error.load");
        }
    }

    public List<Address> getAddresses() {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return addresses;
        }

        String normalizedSearch = searchTerm.trim().toLowerCase(Locale.ROOT);

        return addresses.stream()
                .filter(address -> contains(address.getStreetName(), normalizedSearch)
                        || contains(address.getStreetNumber(), normalizedSearch)
                        || contains(address.getBoxNumber(), normalizedSearch)
                        || contains(address.getCity() != null ? address.getCity().getCityName() : null, normalizedSearch)
                        || contains(address.getCity() != null ? String.valueOf(address.getCity().getZipCode()) : null, normalizedSearch))
                .collect(Collectors.toList());
    }

    private boolean contains(String value, String search) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(search);
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public List<Address> getAllAddresses() {
        return addresses;
    }
}
