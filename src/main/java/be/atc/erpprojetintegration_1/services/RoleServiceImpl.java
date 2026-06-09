package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Role;
import be.atc.erpprojetintegration_1.interfaces.IRoleService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RoleServiceImpl implements IRoleService {

    private static final Logger log = Logger.getLogger(RoleServiceImpl.class);

    @Override
    public Result<List<Role>> getAllActive() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active roles");

            List<Role> roles = em
                    .createNamedQuery("getAllActiveRoles", Role.class)
                    .getResultList();

            log.info("Active roles found: " + roles.size());
            return Result.ok(roles);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.error.load");

            log.error("Error while searching all active roles", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Role> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching role by id: " + id);
            Role role = em.find(Role.class, id);

            if (role == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "roles.error.notFound");

                log.warn("No role found with id: " + id);
                return Result.fail(errors);
            }

            log.info("Role found with id: " + id);
            return Result.ok(role);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.error.load");

            log.error("Error while searching role by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Role> create(Role role) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating role");

            em.getTransaction().begin();
            em.persist(role);
            em.getTransaction().commit();

            log.info("Role created with id: " + role.getId());
            return Result.ok(role);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "roles.create.error");

            log.error("Error while creating role", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
