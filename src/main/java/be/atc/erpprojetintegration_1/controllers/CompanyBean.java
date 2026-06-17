package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.CompanyBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.entities.Company;
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

@Named
@ViewScoped
public class CompanyBean implements Serializable {

    private static final Logger log = Logger.getLogger(CompanyBean.class);

    @Inject
    private CompanyBusiness companyBusiness;
    @Inject
    private EmployeeBusiness employeeBusiness;

    private Company company;
    private boolean editMode;
    private List<City> cities;
    private City selectedCity;

    /**
     * Loads the active company when the page is opened.
     */
    @PostConstruct
    public void init() {
        loadCities();
        loadCompany();
    }

    /**
     * Enables company edition mode.
     */
    public void edit() {
        prepareAddressForEdit();
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
            selectedCity = findCityById(company.getAddress() != null && company.getAddress().getCity() != null
                    ? company.getAddress().getCity().getId()
                    : null);
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

    /**
     * Provides city suggestions for the company address autocomplete field.
     *
     * @param query user search text
     * @return matching city list
     */
    public List<City> completeCity(String query) {
        List<City> results = new ArrayList<>();

        if (cities == null || query == null) {
            return results;
        }

        String search = query.trim().toLowerCase(Locale.ROOT);

        for (City city : cities) {
            String cityName = city.getCityName() != null ? city.getCityName().toLowerCase(Locale.ROOT) : "";
            String zipCode = city.getZipCode() != null ? city.getZipCode().toString() : "";

            if (cityName.contains(search) || zipCode.contains(search)) {
                results.add(city);
            }

            if (results.size() >= 20) {
                break;
            }
        }

        return results;
    }

    private void loadCities() {
        Result<List<City>> result = employeeBusiness.getActiveCities();

        if (result.isSuccess()) {
            cities = result.getData();
            log.info("Cities loaded for company address form: " + cities.size());
        } else {
            cities = new ArrayList<>();
            log.warn("Cities loading failed for company address form");
            MessageUtils.addErrorMessages(result, "employee.address.error.cities.load");
        }
    }

    private void prepareAddressForEdit() {
        if (company == null) {
            return;
        }

        if (company.getAddress() == null) {
            Address address = new Address();
            address.setIsActive(true);
            company.setAddress(address);
        }

        selectedCity = findCityById(company.getAddress().getCity() != null
                ? company.getAddress().getCity().getId()
                : null);
    }

    private City findCityById(Integer cityId) {
        if (cityId == null || cities == null) {
            return null;
        }

        for (City city : cities) {
            if (cityId.equals(city.getId())) {
                return city;
            }
        }

        return null;
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

    public City getSelectedCity() {
        return selectedCity;
    }

    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;

        if (company != null && company.getAddress() != null) {
            company.getAddress().setCity(selectedCity);
        }
    }
}
