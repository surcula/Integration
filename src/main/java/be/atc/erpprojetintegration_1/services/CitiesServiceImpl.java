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
}
