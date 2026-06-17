package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.interfaces.ICitiesService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CityBusiness {

    @Inject
    private ICitiesService citiesService;

    /**
     * Retrieves all cities for the administration list.
     *
     * @return city list result
     */
    public Result<List<City>> getAllCities() {
        return citiesService.getAll();
    }

    /**
     * Retrieves a city by id.
     *
     * @param id city id
     * @return city result
     */
    public Result<City> getCityById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "cities.error.id.required");
            return Result.fail(errors);
        }

        return citiesService.getById(id);
    }

    /**
     * Creates or updates a city after validation.
     *
     * @param city city to save
     * @return saved city result
     */
    public Result<City> saveCity(City city) {
        Result<Void> validationResult = validateCity(city);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        city.setCityName(trim(city.getCityName()));

        if (city.getId() == null) {
            city.setIsActive(true);
            return citiesService.create(city);
        }

        return citiesService.update(city);
    }

    /**
     * Updates city active status.
     *
     * @param id city id
     * @param active active status
     * @return operation result
     */
    public Result<Void> setActive(Integer id, boolean active) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "cities.error.id.required");
            return Result.fail(errors);
        }

        return citiesService.setActive(id, active);
    }

    private Result<Void> validateCity(City city) {
        Map<String, String> errors = new HashMap<>();

        if (city == null) {
            errors.put("city", "cities.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(city.getCityName(), "cityName", "cities.error.name.required", errors);
        FormValidator.lengthBetween(city.getCityName(), "cityName", "cities.error.name.length", 1, 100, errors);

        if (city.getZipCode() == null) {
            errors.put("zipCode", "cities.error.zipCode.required");
        } else if (city.getZipCode() < 1000 || city.getZipCode() > 9999) {
            errors.put("zipCode", "cities.error.zipCode.invalid");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
