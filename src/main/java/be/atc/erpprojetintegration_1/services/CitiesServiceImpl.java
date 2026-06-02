package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.entities.City;
import be.atc.erpprojetintegration_1.interfaces.ICitiesService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CitiesServiceImpl implements ICitiesService {


    /**
     * @return un Resultat ou une erreur. dans la liste des villes,
     *      *
     */
    @Override
    public Result<List<City>> getAllActiveCities() {
        EntityManager em = EMF.getEM();

        try{
            List<City> cities = em.createNamedQuery("getAllActiveCities", City.class).getResultList();
            //LOG
            return Result.ok(cities);
        }catch(Exception ex){
            Map<String, String> errors = new HashMap<>();
            errors.put("message", ex.getMessage());
            //LOG
            return Result.fail(errors);
        }
    }

    @Override
    public Result<List<City>> getActiveByZip(int zip) {
        return null;
    }
}
