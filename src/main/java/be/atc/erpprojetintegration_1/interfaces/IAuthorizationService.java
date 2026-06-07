package be.atc.erpprojetintegration_1.interfaces;

import be.atc.erpprojetintegration_1.tools.Result;

import java.util.List;

public interface IAuthorizationService {
    Result<List<String>> getAuthorizationNamesByRoleId(Integer roleId);
}
