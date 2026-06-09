package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.entities.Authorization;
import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IAuthorizationService {

    /**
     * Retrieves authorization names for a role.
     *
     * @param roleId role id
     * @return authorization name list result
     */
    Result<List<String>> getAuthorizationNamesByRoleId(Integer roleId);

    /**
     * Retrieves active authorization entities assigned to a role.
     *
     * @param roleId role id
     * @return active assigned authorization list result
     */
    Result<List<Authorization>> getActiveAuthorizationsByRoleId(Integer roleId);

    /**
     * Retrieves active authorizations ordered by name.
     *
     * @return active authorization list result
     */
    Result<List<Authorization>> getAllActive();

    /**
     * Assigns authorizations to a role.
     *
     * @param roleId role id
     * @param authorizationIds authorization ids
     * @return result
     */
    Result<Void> assignAuthorizationsToRole(Integer roleId, List<Integer> authorizationIds);
}
