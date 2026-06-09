package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.AddressEditDto;
import be.atc.erpprojetintegration_1.dto.EmployeeEditDto;
import be.atc.erpprojetintegration_1.enums.Civilite;
import be.atc.erpprojetintegration_1.enums.EmploymentStatus;
import be.atc.erpprojetintegration_1.enums.Gender;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class EmployeeEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(EmployeeEditBean.class);

    @Inject
    private EmployeeBusiness employeeBusiness;
    @Inject
    private AuthBean authBean;

    private Integer employeeId;
    private EmployeeEditDto employee;
    private List<City> cities;
    private City selectedCity;

    /**
     * Loads the employee edit form or prepares an empty form for creation.
     */
    public void loadEmployee() {
        log.info("Loading employee edit page. Employee id: " + employeeId);
        loadCities();

        if (employeeId == null) {
            if (!authBean.hasPermission("employee:create")) {
                log.warn("Employee creation page access denied");
                MessageUtils.addErrorMessage("employee.create.error.permission");
                return;
            }

            employee = new EmployeeEditDto();
            employee.setAddress(new AddressEditDto());
            log.info("Employee creation form initialized");
            return;
        }

        if (!authBean.hasPermission("employee:edit")) {
            log.warn("Employee edit page access denied for employee id: " + employeeId);
            MessageUtils.addErrorMessage("employee.edit.error.permission");
            return;
        }

        Result<EmployeeEditDto> result = employeeBusiness.getEmployeeForEdit(employeeId);

        if (result.isSuccess()) {
            employee = result.getData();
            selectedCity = findCityById(employee.getAddress().getCityId());
            log.info("Employee edit form loaded for employee id: " + employeeId);
        } else {
            log.warn("Employee edit form loading failed for employee id: " + employeeId);
            MessageUtils.addErrorMessages(result, "employee.edit.error.load");
        }
    }

    /**
     * Saves the basic employee information form for creation or edition.
     *
     * @return JSF navigation outcome
     */
    public String saveBasicInformation() {
        boolean createMode = isCreateMode();
        log.info((createMode ? "Employee creation" : "Employee basic information update")
                + " requested for employee id: " + employeeId);
        Result<EmployeeEditDto> result;

        if (createMode) {
            if (!authBean.hasPermission("employee:create")) {
                log.warn("Employee creation denied");
                MessageUtils.addErrorMessage("employee.create.error.permission");
                return null;
            }

            result = employeeBusiness.createBasicInformation(employee);
        } else {
            if (!authBean.hasPermission("employee:edit")) {
                log.warn("Employee basic information update denied for employee id: " + employeeId);
                MessageUtils.addErrorMessage("employee.edit.error.permission");
                return null;
            }

            result = employeeBusiness.updateBasicInformation(employee);
        }

        if (!result.isSuccess()) {
            log.warn((createMode ? "Employee creation" : "Employee basic information update")
                    + " failed for employee id: " + employeeId);
            MessageUtils.addErrorMessages(result, createMode ? "employee.create.error.save" : "employee.edit.error.save");
            return null;
        }

        employee = result.getData();
        employeeId = employee.getId();
        log.info((createMode ? "Employee created" : "Employee basic information updated")
                + " with id: " + employeeId);
        MessageUtils.addInfoMessage(createMode ? "employee.create.success.save" : "employee.edit.success.save");
        return null;
    }

    /**
     * Saves the employee address form.
     *
     * @return JSF navigation outcome
     */
    public String saveAddress() {
        log.info("Employee address update requested for employee id: " + employeeId);

        if (employeeId == null) {
            log.warn("Employee address update denied because employee id is missing");
            MessageUtils.addErrorMessage("employee.address.error.employee.required");
            return null;
        }

        if (!authBean.hasPermission("employee:edit")) {
            log.warn("Employee address update denied for employee id: " + employeeId);
            MessageUtils.addErrorMessage("employee.edit.error.permission");
            return null;
        }

        Result<EmployeeEditDto> result = employeeBusiness.updateAddress(employeeId, employee.getAddress());

        if (!result.isSuccess()) {
            log.warn("Employee address update failed for employee id: " + employeeId);
            MessageUtils.addErrorMessages(result, "employee.address.error.save");
            return null;
        }

        employee = result.getData();
        selectedCity = findCityById(employee.getAddress().getCityId());
        log.info("Employee address updated for employee id: " + employeeId);
        MessageUtils.addInfoMessage("employee.address.success.save");
        return null;
    }

    /**
     * Provides city suggestions for the address autocomplete field.
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

    /**
     * Loads active cities used by the address autocomplete field.
     */
    private void loadCities() {
        Result<List<City>> result = employeeBusiness.getActiveCities();

        if (result.isSuccess()) {
            cities = result.getData();
            log.info("Cities loaded for employee address form: " + cities.size());
        } else {
            log.warn("Cities loading failed for employee address form");
            MessageUtils.addErrorMessages(result, "employee.address.error.cities.load");
        }
    }

    /**
     * Finds a city from the already loaded city list.
     *
     * @param cityId city id
     * @return matching city or null
     */
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

    /**
     * Indicates if the page is used to create a new employee.
     *
     * @return true in create mode
     */
    public boolean isCreateMode() {
        return employeeId == null;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public EmployeeEditDto getEmployee() {
        return employee;
    }

    public void setEmployee(EmployeeEditDto employee) {
        this.employee = employee;
    }

    public Civilite[] getCivilites() {
        return Civilite.values();
    }

    public Gender[] getGenders() {
        return Gender.values();
    }

    public EmploymentStatus[] getEmploymentStatuses() {
        return EmploymentStatus.values();
    }

    public List<City> getCities() {
        return cities;
    }

    public City getSelectedCity() {
        return selectedCity;
    }

    /**
     * Updates the selected city and stores its id in the address dto.
     *
     * @param selectedCity selected city
     */
    public void setSelectedCity(City selectedCity) {
        this.selectedCity = selectedCity;

        if (employee != null && employee.getAddress() != null) {
            employee.getAddress().setCityId(selectedCity != null ? selectedCity.getId() : null);
        }
    }
}
