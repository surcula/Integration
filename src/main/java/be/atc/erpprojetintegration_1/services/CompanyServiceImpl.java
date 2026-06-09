package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Company;
import be.atc.erpprojetintegration_1.interfaces.ICompanyService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CompanyServiceImpl implements ICompanyService {

    private static final Logger log = Logger.getLogger(CompanyServiceImpl.class);

    @Override
    public Result<Company> getActiveCompany() {
        EntityManager em = EMF.getEM();

        try {
            log.info("Searching active company");

            List<Company> companies = em
                    .createNamedQuery("getActiveCompany", Company.class)
                    .setMaxResults(1)
                    .getResultList();

            if (companies.isEmpty()) {
                Map<String, String> errors = new HashMap<>();
                errors.put("notFound", "company.error.notFound");

                log.warn("No active company found");
                return Result.fail(errors);
            }

            log.info("Active company found with id: " + companies.get(0).getId());
            return Result.ok(companies.get(0));

        } catch (Exception ex) {
            Map<String, String> errors = new HashMap<>();
            errors.put("message", "company.error.load");

            log.error("Error while searching active company", ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }

    @Override
    public Result<Company> update(Company company) {
        EntityManager em = EMF.getEM();

        try {
            log.info("Updating company id: " + company.getId());

            em.getTransaction().begin();
            Company updatedCompany = em.merge(company);
            em.getTransaction().commit();

            log.info("Company updated with id: " + updatedCompany.getId());
            return Result.ok(updatedCompany);

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            Map<String, String> errors = new HashMap<>();
            errors.put("message", "company.error.save");

            log.error("Error while updating company id: " + company.getId(), ex);
            return Result.fail(errors);

        } finally {
            em.close();
        }
    }
}
