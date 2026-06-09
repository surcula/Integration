package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.interfaces.IAddressService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class AddressServiceImpl implements IAddressService {

    private static final Logger log = Logger.getLogger(AddressServiceImpl.class);

    @Override
    public Result<Address> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching address by id: " + id);
            Address address = em.find(Address.class, id);

            if (address == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "employee.address.error.notFound");
                return Result.fail(errors);
            }

            return Result.ok(address);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            log.error("Error while searching address by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Address> create(Address address) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating address");
            em.getTransaction().begin();
            em.persist(address);
            em.getTransaction().commit();

            return Result.ok(address);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            log.error("Error while creating address", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Address> update(Address address) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating address with id: " + address.getId());
            em.getTransaction().begin();
            Address updatedAddress = em.merge(address);
            em.getTransaction().commit();

            return Result.ok(updatedAddress);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            log.error("Error while updating address with id: " + address.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
