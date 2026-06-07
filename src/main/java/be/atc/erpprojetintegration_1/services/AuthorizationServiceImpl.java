package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.interfaces.IAuthorizationService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AuthorizationServiceImpl implements IAuthorizationService {

    // Log4j
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
            errors.put("message", ex.getMessage());

            log.error("Error while searching authorization names by role id: " + roleId, ex);

            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
