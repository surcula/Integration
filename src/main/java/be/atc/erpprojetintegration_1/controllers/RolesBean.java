package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.RoleBusiness;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.tools.MessageUtils;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@RequestScoped
public class RolesBean implements Serializable {

    private static final Logger log = Logger.getLogger(RolesBean.class);

    @Inject
    private RoleBusiness roleBusiness;

    private List<Role> roles;

    /**
     * Loads active roles when the page is initialized.
     */
    @PostConstruct
    public void init() {
        loadRoles();
    }

    /**
     * Loads active roles for the role list page.
     */
    private void loadRoles() {
        log.info("Loading role list");

        Result<List<Role>> result = roleBusiness.getActiveRoles();

        if (result.isSuccess()) {
            roles = result.getData();
            log.info("Role list loaded: " + roles.size() + " role(s)");
        } else {
            roles = new ArrayList<>();
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
        return roles;
    }
}
