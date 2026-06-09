package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Function;
import be.atc.erpprojetintegration_1.interfaces.IFunctionService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FunctionServiceImpl implements IFunctionService {

    private static final Logger log = Logger.getLogger(FunctionServiceImpl.class);

    @Override
    public Result<List<Function>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all functions");
            List<Function> functions = em.createNamedQuery("getAllFunctions", Function.class).getResultList();
            log.info("Functions found: " + functions.size());
            return Result.ok(functions);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "functions.error.load");
            log.error("Error while searching all functions", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Function> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching function by id: " + id);
            Function function = em.find(Function.class, id);

            if (function == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "functions.error.notFound");
                log.warn("No function found with id: " + id);
                return Result.fail(errors);
            }

            log.info("Function found with id: " + id);
            return Result.ok(function);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "functions.error.load");
            log.error("Error while searching function by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Function> create(Function function) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating function");
            em.getTransaction().begin();
            em.persist(function);
            em.getTransaction().commit();
            log.info("Function created with id: " + function.getId());
            return Result.ok(function);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "functions.error.save");
            log.error("Error while creating function", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Function> update(Function function) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating function id: " + function.getId());
            em.getTransaction().begin();
            Function updatedFunction = em.merge(function);
            em.getTransaction().commit();
            log.info("Function updated with id: " + updatedFunction.getId());
            return Result.ok(updatedFunction);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "functions.error.save");
            log.error("Error while updating function id: " + function.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating function active status. Id: " + id + ", active: " + active);
            em.getTransaction().begin();
            Function function = em.find(Function.class, id);

            if (function == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "functions.error.notFound");
                log.warn("Cannot update function active status. Function not found with id: " + id);
                return Result.fail(errors);
            }

            function.setIsActive(active);
            em.merge(function);
            em.getTransaction().commit();
            log.info("Function active status updated. Id: " + id + ", active: " + active);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", active ? "functions.activate.error" : "functions.delete.error");
            log.error("Error while updating function active status. Id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
