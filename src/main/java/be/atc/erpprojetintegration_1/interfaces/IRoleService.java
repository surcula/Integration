package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

/**
 * Defines role-related database operations.
 */
public interface IRoleService {

    /**
     * Retrieves active roles ordered by name.
     *
     * @return active role list
     */
    Result<List<Role>> getAllActive();

    /**
     * Retrieves a role by id.
     *
     * @param id role id
     * @return role result
     */
    Result<Role> getById(Integer id);

    /**
     * Creates a role.
     *
     * @param role role to create
     * @return created role result
     */
    Result<Role> create(Role role);
}
