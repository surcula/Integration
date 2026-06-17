package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Address;
import be.atc.erpprojetintegration_1.interfaces.IAddressService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class AddressServiceImpl implements IAddressService {

    private static final Logger log = Logger.getLogger(AddressServiceImpl.class);

    @Override
    public Result<List<Address>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all addresses");
            List<Address> addresses = em.createNamedQuery("getAllAddresses", Address.class).getResultList();
            log.info("Addresses found: " + addresses.size());
            return Result.ok(addresses);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "addresses.error.load");
            log.error("Error while searching all addresses", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

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
            address.setCity(em.merge(address.getCity()));
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
            address.setCity(em.merge(address.getCity()));
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

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating address active status. Id: " + id + ", active: " + active);
            em.getTransaction().begin();
            Address address = em.find(Address.class, id);

            if (address == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "addresses.error.notFound");
                log.warn("Cannot update address active status. Address not found with id: " + id);
                return Result.fail(errors);
            }

            address.setIsActive(active);
            em.merge(address);
            em.getTransaction().commit();
            log.info("Address active status updated. Id: " + id + ", active: " + active);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", active ? "addresses.activate.error" : "addresses.delete.error");
            log.error("Error while updating address active status. Id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
