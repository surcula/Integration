package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IAddressService {

    /**
     * Retrieves all addresses.
     *
     * @return address list result
     */
    Result<List<Address>> getAll();

    /**
     * Retrieves an address by id.
     *
     * @param id address id
     * @return address result
     */
    Result<Address> getById(Integer id);

    /**
     * Creates a new address.
     *
     * @param address address to create
     * @return created address result
     */
    Result<Address> create(Address address);

    /**
     * Updates an existing address.
     *
     * @param address address to update
     * @return updated address result
     */
    Result<Address> update(Address address);

    /**
     * Updates address active status.
     *
     * @param id address id
     * @param active active status
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
