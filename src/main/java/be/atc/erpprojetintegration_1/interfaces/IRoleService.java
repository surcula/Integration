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
     * Retrieves all roles ordered by name.
     *
     * @return complete role list
     */
    Result<List<Role>> getAll();

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

    /**
     * Activates or deactivates a role.
     *
     * @param id role id
     * @param active new active state
     * @return operation result
     */
    Result<Void> setActive(Integer id, boolean active);
}
