package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.interfaces.IAddressService;
import be.atc.erpprojetintegration_1.interfaces.ICitiesService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AddressBusiness {

    @Inject
    private IAddressService addressService;

    @Inject
    private ICitiesService citiesService;

    /**
     * Retrieves all addresses for the administration list.
     *
     * @return address list result
     */
    public Result<List<Address>> getAllAddresses() {
        return addressService.getAll();
    }

    /**
     * Retrieves an address by id.
     *
     * @param id address id
     * @return address result
     */
    public Result<Address> getAddressById(Integer id) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "addresses.error.id.required");
            return Result.fail(errors);
        }

        return addressService.getById(id);
    }

    /**
     * Creates or updates an address after validation.
     *
     * @param address address to save
     * @return saved address result
     */
    public Result<Address> saveAddress(Address address) {
        Result<Void> validationResult = validateAddress(address);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        trimAddressFields(address);

        if (address.getId() == null) {
            address.setIsActive(true);
            return addressService.create(address);
        }

        return addressService.update(address);
    }

    /**
     * Updates address active status.
     *
     * @param id address id
     * @param active active status
     * @return operation result
     */
    public Result<Void> setActive(Integer id, boolean active) {
        if (id == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("id", "addresses.error.id.required");
            return Result.fail(errors);
        }

        return addressService.setActive(id, active);
    }

    /**
     * Retrieves active cities for the address form.
     *
     * @return active city list result
     */
    public Result<List<City>> getActiveCities() {
        return citiesService.getAllActiveCities();
    }

    private Result<Void> validateAddress(Address address) {
        Map<String, String> errors = new HashMap<>();

        if (address == null) {
            errors.put("address", "addresses.error.form.invalid");
            return Result.fail(errors);
        }

        FormValidator.required(address.getStreetName(), "streetName", "addresses.error.streetName.required", errors);
        FormValidator.lengthBetween(address.getStreetName(), "streetName", "addresses.error.streetName.length", 1, 50, errors);
        FormValidator.lengthBetween(address.getStreetNumber(), "streetNumber", "addresses.error.streetNumber.length", 0, 11, errors);
        FormValidator.lengthBetween(address.getBoxNumber(), "boxNumber", "addresses.error.boxNumber.length", 0, 5, errors);

        if (address.getCity() == null || address.getCity().getId() == null) {
            errors.put("city", "addresses.error.city.required");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }

    private void trimAddressFields(Address address) {
        address.setStreetName(trim(address.getStreetName()));
        address.setStreetNumber(trim(address.getStreetNumber()));
        address.setBoxNumber(trim(address.getBoxNumber()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
