package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.tools.Result;

public interface IAddressService {

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
}
