package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.interfaces.ICitiesService;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CitiesServiceImpl implements ICitiesService {

    private static final Logger log = Logger.getLogger(CitiesServiceImpl.class);

    @Override
    public Result<List<City>> getAll() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all cities");
            List<City> cities = em.createNamedQuery("getAllCities", City.class).getResultList();

            log.info("Cities found: " + cities.size());
            return Result.ok(cities);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "cities.error.load");

            log.error("Error while searching all cities", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    /**
     * @return un Resultat ou une erreur. dans la liste des villes,
     *      *
     */
    @Override
    public Result<List<City>> getAllActiveCities() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching all active cities");
            List<City> cities = em.createNamedQuery("getAllActiveCities", City.class).getResultList();

            log.info("Active cities found: " + cities.size());
            return Result.ok(cities);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching all active cities", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<City>> getActiveByZip(int zip) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active cities by zip: " + zip);
            List<City> cities = em.createNamedQuery("getActiveCitiesByZip", City.class)
                    .setParameter("zip", zip)
                    .getResultList();

            log.info("Active cities found for zip " + zip + ": " + cities.size());
            return Result.ok(cities);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching active cities by zip: " + zip, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<City> getById(Integer id) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching city by id: " + id);
            City city = em.find(City.class, id);

            if (city == null) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "employee.address.error.city.notFound");

                log.warn("No city found with id: " + id);
                return Result.fail(errors);
            }

            log.info("City found with id: " + id);
            return Result.ok(city);

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());

            log.error("Error while searching city by id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<City> create(City city) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Creating city");
            em.getTransaction().begin();
            em.persist(city);
            em.getTransaction().commit();
            log.info("City created with id: " + city.getId());
            return Result.ok(city);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "cities.error.save");
            log.error("Error while creating city", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<City> update(City city) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating city id: " + city.getId());
            em.getTransaction().begin();
            City updatedCity = em.merge(city);
            em.getTransaction().commit();
            log.info("City updated with id: " + updatedCity.getId());
            return Result.ok(updatedCity);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "cities.error.save");
            log.error("Error while updating city id: " + city.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating city active status. Id: " + id + ", active: " + active);
            em.getTransaction().begin();
            City city = em.find(City.class, id);

            if (city == null) {
                em.getTransaction().rollback();
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "cities.error.notFound");
                log.warn("Cannot update city active status. City not found with id: " + id);
                return Result.fail(errors);
            }

            city.setIsActive(active);
            em.merge(city);
            em.getTransaction().commit();
            log.info("City active status updated. Id: " + id + ", active: " + active);
            return Result.ok();

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", active ? "cities.activate.error" : "cities.delete.error");
            log.error("Error while updating city active status. Id: " + id, ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
