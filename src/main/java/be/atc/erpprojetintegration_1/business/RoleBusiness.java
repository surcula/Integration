package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Authorization;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.interfaces.IAuthorizationService;
import be.atc.erpprojetintegration_1.interfaces.IRoleService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleBusiness {

    @Inject
    private IRoleService roleService;
    @Inject
    private IAuthorizationService authorizationService;

    /**
     * Retrieves active roles for the role administration page.
     *
     * @return active role list result
     */
    public Result<List<Role>> getActiveRoles() {
        return roleService.getAllActive();
    }

    /**
     * Retrieves a role by id.
     *
     * @param roleId role id
     * @return role result
     */
    public Result<Role> getRoleById(Integer roleId) {
        if (roleId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("roleId", "roles.error.id.required");
            return Result.fail(errors);
        }

        return roleService.getById(roleId);
    }

    /**
     * Validates and creates a new role.
     *
     * @param roleName role name
     * @return created role result
     */
    public Result<Role> createRole(String roleName) {
        Result<Void> validationResult = validateRoleForm(roleName);

        if (!validationResult.isSuccess()) {
            return Result.fail(validationResult.getErrors());
        }

        Role role = new Role();
        role.setRoleName(roleName.trim().toUpperCase());
        role.setIsActive(true);

        return roleService.create(role);
    }

    /**
     * Retrieves active authorizations used by the role form.
     *
     * @return active authorization list result
     */
    public Result<List<Authorization>> getActiveAuthorizations() {
        return authorizationService.getAllActive();
    }

    /**
     * Retrieves active authorizations already assigned to a role.
     *
     * @param roleId role id
     * @return assigned authorization list result
     */
    public Result<List<Authorization>> getActiveAuthorizationsByRoleId(Integer roleId) {
        if (roleId == null) {
            Map<String, String> errors = new HashMap<>();
            errors.put("roleId", "roles.error.id.required");
            return Result.fail(errors);
        }

        return authorizationService.getActiveAuthorizationsByRoleId(roleId);
    }

    /**
     * Assigns selected authorizations to an existing role.
     *
     * @param roleId role id
     * @param selectedAuthorizations selected authorizations
     * @return result
     */
    public Result<Void> assignAuthorizations(Integer roleId, List<Authorization> selectedAuthorizations) {
        Map<String, String> errors = new HashMap<>();

        if (roleId == null) {
            errors.put("roleId", "roles.error.id.required");
        }

        if (selectedAuthorizations == null || selectedAuthorizations.isEmpty()) {
            errors.put("authorizations", "roles.authorizations.error.required");
        }

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        List<Integer> ids = selectedAuthorizations.stream()
                .map(Authorization::getId)
                .collect(Collectors.toList());

        return authorizationService.assignAuthorizationsToRole(roleId, ids);
    }

    /**
     * Validates the role creation form.
     *
     * @param roleName role name
     * @return validation result
     */
    private Result<Void> validateRoleForm(String roleName) {
        Map<String, String> errors = new HashMap<>();

        FormValidator.required(roleName, "roleName", "roles.error.name.required", errors);
        FormValidator.lengthBetween(roleName, "roleName", "roles.error.name.length", 2, 100, errors);

        if (!errors.isEmpty()) {
            return Result.fail(errors);
        }

        return Result.ok();
    }
}
