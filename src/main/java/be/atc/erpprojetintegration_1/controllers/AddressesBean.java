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

@Named
@ViewScoped
public class AddressesBean implements Serializable {

    private static final Logger log = Logger.getLogger(AddressesBean.class);

    @Inject
    private AddressBusiness addressBusiness;

    private List<Address> addresses;

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
        return addresses;
    }
}
