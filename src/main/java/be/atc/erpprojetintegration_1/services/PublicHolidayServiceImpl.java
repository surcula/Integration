package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.PublicHoliday;
import be.atc.erpprojetintegration_1.interfaces.IPublicHolidayService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PublicHolidayServiceImpl implements IPublicHolidayService {

    private static final Logger log = Logger.getLogger(PublicHolidayServiceImpl.class);

    @Override
    public Result<List<PublicHoliday>> getAll() {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getAllPublicHolidays", PublicHoliday.class).getResultList());
        } catch (Exception ex) {
            log.error("Error while loading public holidays", ex);
            return Result.fail(error("publicHolidays.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<PublicHoliday> save(PublicHoliday publicHoliday) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PublicHoliday saved;
            if (publicHoliday.getId() == null) {
                em.persist(publicHoliday);
                saved = publicHoliday;
            } else {
                saved = em.merge(publicHoliday);
            }
            em.getTransaction().commit();
            return Result.ok(saved);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.error("Error while saving public holiday", ex);
            return Result.fail(error("publicHolidays.error.save"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> saveAll(List<PublicHoliday> publicHolidays) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            for (PublicHoliday publicHoliday : publicHolidays) {
                List<PublicHoliday> existing = em.createNamedQuery("getPublicHolidayByDateAndName", PublicHoliday.class)
                        .setParameter("holidayDate", publicHoliday.getHolidayDate())
                        .setParameter("name", publicHoliday.getName())
                        .getResultList();

                if (existing.isEmpty()) {
                    em.persist(publicHoliday);
                } else {
                    PublicHoliday savedHoliday = existing.get(0);
                    savedHoliday.setDescription(publicHoliday.getDescription());
                    savedHoliday.setIsActive(true);
                }
            }
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.error("Error while generating public holidays", ex);
            return Result.fail(error("publicHolidays.error.generate"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            PublicHoliday publicHoliday = em.find(PublicHoliday.class, id);
            if (publicHoliday == null) {
                em.getTransaction().rollback();
                return Result.fail(error("publicHolidays.error.notFound"));
            }
            publicHoliday.setIsActive(active);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            log.error("Error while updating public holiday status", ex);
            return Result.fail(error("publicHolidays.error.save"));
        } finally {
            em.close();
        }
    }

    private Map<String, String> error(String key) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", key);
        return errors;
    }
}
