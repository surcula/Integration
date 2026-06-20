package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.RoleBusiness;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class RolesBean implements Serializable {

    private static final Logger log = Logger.getLogger(RolesBean.class);

    @Inject
    private RoleBusiness roleBusiness;

    private List<Role> roles;
    private List<Role> displayedRoles;
    private boolean activeOnly;

    /**
     * Loads all roles when the page is initialized.
     */
    @PostConstruct
    public void init() {
        loadRoles();
    }

    /**
     * Loads all roles for the role list page.
     */
    private void loadRoles() {
        log.info("Loading role list");

        Result<List<Role>> result = roleBusiness.getAllRoles();

        if (result.isSuccess()) {
            roles = result.getData();
            applyActiveFilter();
            log.info("Role list loaded: " + roles.size() + " role(s)");
        } else {
            roles = new ArrayList<>();
            displayedRoles = new ArrayList<>();
            log.warn("Role list loading failed");
            MessageUtils.addErrorMessages(result, "roles.error.load");
        }
    }

    /**
     * Returns roles displayed by the role list page.
     *
     * @return active role list
     */
    public List<Role> getRoles() {
        return displayedRoles;
    }

    /**
     * Enables or disables the active-role filter.
     */
    public void toggleActiveFilter() {
        activeOnly = !activeOnly;
        applyActiveFilter();
    }

    /**
     * Soft deletes or reactivates a role and refreshes the table.
     *
     * @param roleId role id
     * @param currentlyActive current role state
     */
    public void changeRoleActiveStatus(Integer roleId, boolean currentlyActive) {
        Result<Void> result = roleBusiness.setRoleActive(roleId, !currentlyActive);

        if (!result.isSuccess()) {
            MessageUtils.addErrorMessages(result, currentlyActive ? "roles.delete.error" : "roles.activate.error");
            return;
        }

        MessageUtils.addInfoMessage(currentlyActive ? "roles.delete.success" : "roles.activate.success");
        loadRoles();
    }

    private void applyActiveFilter() {
        if (roles == null) {
            displayedRoles = new ArrayList<>();
            return;
        }

        displayedRoles = roles.stream()
                .filter(role -> !activeOnly || Boolean.TRUE.equals(role.getIsActive()))
                .collect(Collectors.toList());
    }

    public boolean isActiveOnly() {
        return activeOnly;
    }

    public long getActiveRoleCount() {
        return roles == null ? 0 : roles.stream()
                .filter(role -> Boolean.TRUE.equals(role.getIsActive()))
                .count();
    }
}
