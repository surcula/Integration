package be.atc.erpprojetintegration_1.interfaces;

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
}
