package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Authorization;
import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.entities.RolesAuthorization;
import be.atc.erpprojetintegration_1.interfaces.IAuthorizationService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AuthorizationServiceImpl implements IAuthorizationService {

    private static final Logger log = Logger.getLogger(AuthorizationServiceImpl.class);

    @Override
    public Result<List<String>> getAuthorizationNamesByRoleId(Integer roleId) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching authorization names by role id: " + roleId);

            List<String> authorizations = em
                    .createNamedQuery("getAuthorizationNamesByRoleId", String.class)
                    .setParameter("roleId", roleId)
                    .getResultList();

            log.info("Found " + authorizations.size() + " authorization(s) for role id: " + roleId);
            return Result.ok(authorizations);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.authorizations.error.load");

            log.error("Error while searching authorization names by role id: " + roleId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Authorization>> getActiveAuthorizationsByRoleId(Integer roleId) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active authorizations by role id: " + roleId);

            List<Authorization> authorizations = em
                    .createNamedQuery("getActiveAuthorizationsByRoleId", Authorization.class)
                    .setParameter("roleId", roleId)
                    .getResultList();

            log.info("Found " + authorizations.size() + " active authorization(s) for role id: " + roleId);
            return Result.ok(authorizations);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.authorizations.error.load");

            log.error("Error while searching active authorizations by role id: " + roleId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<Authorization>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active authorizations");

            List<Authorization> authorizations = em
                    .createNamedQuery("getAllActiveAuthorizations", Authorization.class)
                    .getResultList();

            log.info("Active authorizations found: " + authorizations.size());
            return Result.ok(authorizations);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.authorizations.error.load");

            log.error("Error while searching all active authorizations", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> assignAuthorizationsToRole(Integer roleId, List<Integer> authorizationIds) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Synchronizing authorizations for role id: " + roleId);

            em.getTransaction().begin();
            Role role = em.find(Role.class, roleId);

            if (role == null) {
                em.getTransaction().rollback();

                Map<String, String> errors = new HashMap<>();
                errors.put("role", "roles.error.notFound");

                log.warn("Cannot assign authorizations. Role not found with id: " + roleId);
                return Result.fail(errors);
            }

            Set<Integer> selectedIds = new HashSet<>(authorizationIds);
            Set<Integer> existingAuthorizationIds = new HashSet<>();

            List<RolesAuthorization> existingRoleAuthorizations = em
                    .createNamedQuery("getRoleAuthorizationsByRoleId", RolesAuthorization.class)
                    .setParameter("roleId", roleId)
                    .getResultList();

            for (RolesAuthorization roleAuthorization : existingRoleAuthorizations) {
                Integer authorizationId = roleAuthorization.getAuthorization().getId();
                existingAuthorizationIds.add(authorizationId);

                boolean isSelected = selectedIds.contains(authorizationId);
                roleAuthorization.setIsActive(isSelected);
                em.merge(roleAuthorization);
            }

            for (Integer authorizationId : authorizationIds) {
                if (authorizationId == null) {
                    continue;
                }

                if (existingAuthorizationIds.contains(authorizationId)) {
                    continue;
                }

                Authorization authorization = em.find(Authorization.class, authorizationId);

                if (authorization == null) {
                    log.warn("Authorization not found with id: " + authorizationId);
                    continue;
                }

                RolesAuthorization roleAuthorization = new RolesAuthorization();
                roleAuthorization.setRole(role);
                roleAuthorization.setAuthorization(authorization);
                roleAuthorization.setRoleAuthorizationName(buildRoleAuthorizationName(role, authorization));
                roleAuthorization.setIsActive(true);
                em.persist(roleAuthorization);
            }

            em.getTransaction().commit();

            log.info("Authorizations synchronized for role id: " + roleId);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.authorizations.error.save");

            log.error("Error while synchronizing authorizations for role id: " + roleId, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    private String buildRoleAuthorizationName(Role role, Authorization authorization) {
        return role.getRoleName() + "_" + authorization.getAuthorizationName().replace(':', '_');
    }
}
