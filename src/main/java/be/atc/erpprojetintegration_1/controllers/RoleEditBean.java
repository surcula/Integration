package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.RoleBusiness;
import be.atc.erpprojetintegration_1.entities.Authorization;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;
import org.primefaces.model.DualListModel;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class RoleEditBean implements Serializable {

    private static final Logger log = Logger.getLogger(RoleEditBean.class);

    @Inject
    private RoleBusiness roleBusiness;

    private Integer roleId;
    private String roleName;
    private Role role;
    private DualListModel<Authorization> authorizationPickList;

    /**
     * Loads the role form and available authorizations.
     */
    public void loadRole() {
        log.info("Loading role edit page. Role id: " + roleId);

        if (roleId == null) {
            role = null;
            loadAuthorizations(new ArrayList<>());
            return;
        }

        Result<Role> roleResult = roleBusiness.getRoleById(roleId);

        if (roleResult.isSuccess()) {
            role = roleResult.getData();
            roleName = role.getRoleName();
            loadAssignedAuthorizations();
            log.info("Role edit page loaded for role id: " + roleId);
        } else {
            log.warn("Role loading failed for role id: " + roleId);
            MessageUtils.addErrorMessages(roleResult, "roles.error.load");
            loadAuthorizations(new ArrayList<>());
        }
    }

    /**
     * Creates the role before assigning authorizations.
     *
     * @return JSF navigation outcome
     */
    public String createRole() {
        log.info("Role creation requested");

        Result<Role> result = roleBusiness.createRole(roleName);

        if (!result.isSuccess()) {
            log.warn("Role creation failed");
            MessageUtils.addErrorMessages(result, "roles.create.error");
            return null;
        }

        role = result.getData();
        roleId = role.getId();
        roleName = role.getRoleName();

        log.info("Role created from page with id: " + roleId);
        MessageUtils.addInfoMessage("roles.create.success");
        return null;
    }

    /**
     * Assigns selected authorizations to the current role.
     *
     * @return JSF navigation outcome
     */
    public String saveAuthorizations() {
        log.info("Role authorization assignment requested for role id: " + roleId);

        Result<Void> result = roleBusiness.assignAuthorizations(roleId, authorizationPickList.getTarget());

        if (!result.isSuccess()) {
            log.warn("Role authorization assignment failed for role id: " + roleId);
            MessageUtils.addErrorMessages(result, "roles.authorizations.error.save");
            return null;
        }

        log.info("Role authorizations assigned for role id: " + roleId);
        MessageUtils.addInfoMessage("roles.authorizations.success.save");
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
        return "/views/roles?faces-redirect=true";
    }

    private void loadAssignedAuthorizations() {
        Result<List<Authorization>> result = roleBusiness.getActiveAuthorizationsByRoleId(roleId);

        if (result.isSuccess()) {
            loadAuthorizations(new ArrayList<>(result.getData()));
            log.info("Assigned authorizations loaded for role id " + roleId + ": " + result.getData().size());
        } else {
            loadAuthorizations(new ArrayList<>());
            log.warn("Assigned authorizations loading failed for role id: " + roleId);
            MessageUtils.addErrorMessages(result, "roles.authorizations.error.load");
        }
    }

    private void loadAuthorizations(List<Authorization> assignedAuthorizations) {
        Result<List<Authorization>> result = roleBusiness.getActiveAuthorizations();

        if (result.isSuccess()) {
            List<Authorization> availableAuthorizations = new ArrayList<>(result.getData());
            availableAuthorizations.removeIf(authorization ->
                    containsAuthorization(assignedAuthorizations, authorization.getId()));

            authorizationPickList = new DualListModel<>(availableAuthorizations, assignedAuthorizations);
            log.info("Authorizations loaded for role form: " + result.getData().size());
        } else {
            authorizationPickList = new DualListModel<>(new ArrayList<>(), new ArrayList<>());
            log.warn("Authorizations loading failed for role form");
            MessageUtils.addErrorMessages(result, "roles.authorizations.error.load");
        }
    }

    private boolean containsAuthorization(List<Authorization> authorizations, Integer authorizationId) {
        if (authorizations == null || authorizationId == null) {
            return false;
        }

        return authorizations.stream()
                .anyMatch(authorization -> authorizationId.equals(authorization.getId()));
    }

    public boolean isCreateMode() {
        return roleId == null;
    }

    public boolean isRoleCreated() {
        return roleId != null;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public DualListModel<Authorization> getAuthorizationPickList() {
        return authorizationPickList;
    }

    public void setAuthorizationPickList(DualListModel<Authorization> authorizationPickList) {
        this.authorizationPickList = authorizationPickList;
    }
}
