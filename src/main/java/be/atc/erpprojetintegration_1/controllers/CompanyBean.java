package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.CompanyBusiness;
import be.atc.erpprojetintegration_1.entities.Company;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@ViewScoped
public class CompanyBean implements Serializable {

    private static final Logger log = Logger.getLogger(CompanyBean.class);

    @Inject
    private CompanyBusiness companyBusiness;

    private Company company;
    private boolean editMode;

    /**
     * Loads the active company when the page is opened.
     */
    @PostConstruct
    public void init() {
        loadCompany();
    }

    /**
     * Enables company edition mode.
     */
    public void edit() {
        editMode = true;
    }

    /**
     * Cancels edition and reloads persisted company data.
     */
    public void cancel() {
        editMode = false;
        loadCompany();
    }

    /**
     * Saves company information.
     */
    public void save() {
        Result<Company> result = companyBusiness.updateCompany(company);

        if (!result.isSuccess()) {
            log.warn("Company update failed");
            MessageUtils.addErrorMessages(result, "company.error.save");
            return;
        }

        company = result.getData();
        editMode = false;
        log.info("Company saved from administration page");
        MessageUtils.addInfoMessage("company.success.save");
    }

    private void loadCompany() {
        Result<Company> result = companyBusiness.getActiveCompany();

        if (result.isSuccess()) {
            company = result.getData();
            log.info("Company loaded in administration page");
        } else {
            company = null;
            log.warn("Company loading failed in administration page");
            MessageUtils.addErrorMessages(result, "company.error.load");
        }
    }

    public Company getCompany() {
        return company;
    }

    public String getCompanyAddress() {
        if (company == null || company.getAddress() == null) {
            return "";
        }

        StringBuilder address = new StringBuilder();

        append(address, company.getAddress().getStreetName());
        append(address, company.getAddress().getStreetNumber());
        append(address, company.getAddress().getBoxNumber());

        if (company.getAddress().getCity() != null) {
            append(address, String.valueOf(company.getAddress().getCity().getZipCode()));
            append(address, company.getAddress().getCity().getCityName());
        }

        return address.toString();
    }

    private void append(StringBuilder builder, String value) {
        if (value == null || value.trim().isEmpty()) {
            return;
        }

        if (builder.length() > 0) {
            builder.append(" ");
        }

        builder.append(value.trim());
    }

    public boolean isEditMode() {
        return editMode;
    }
}
