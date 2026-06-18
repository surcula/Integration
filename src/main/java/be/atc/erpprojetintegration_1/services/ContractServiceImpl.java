package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Contract;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.enums.ContractStatus;
import be.atc.erpprojetintegration_1.interfaces.IContractService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ContractServiceImpl implements IContractService {
    private static final Logger log = Logger.getLogger(ContractServiceImpl.class);

    @Override
    public Result<List<Contract>> getAll() { return list("getAllContracts", null); }

    @Override
    public Result<List<Contract>> getByEmployee(Integer employeeId) {
        return list("getContractsByEmployee", employeeId);
    }

    private Result<List<Contract>> list(String queryName, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            TypedQuery<Contract> query = em.createNamedQuery(queryName, Contract.class);
            if (employeeId != null) query.setParameter("employeeId", employeeId);
            return Result.ok(query.getResultList());
        } catch (Exception ex) {
            log.error("Error while loading contracts", ex);
            return Result.fail(error("Impossible de charger les contrats."));
        } finally { em.close(); }
    }

    @Override
    public Result<Contract> getActive(Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            List<Contract> rows = em.createNamedQuery("getActiveContractByEmployee", Contract.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("active", ContractStatus.ACTIVE)
                    .setMaxResults(1).getResultList();
            return Result.ok(rows.isEmpty() ? null : rows.get(0));
        } catch (Exception ex) {
            log.error("Error while loading active contract", ex);
            return Result.fail(error("Impossible de charger le contrat actif."));
        } finally { em.close(); }
    }

    @Override
    public Result<Contract> getById(Integer contractId) {
        EntityManager em = EMF.getEM();
        try {
            List<Contract> rows = em.createNamedQuery("getContractById", Contract.class)
                    .setParameter("contractId", contractId).setMaxResults(1).getResultList();
            return Result.ok(rows.isEmpty() ? null : rows.get(0));
        } catch (Exception ex) {
            log.error("Error while loading contract", ex);
            return Result.fail(error("Impossible de charger le contrat."));
        } finally { em.close(); }
    }

    @Override
    public Result<Contract> create(Contract contract, Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            Long activeCount = em.createNamedQuery("countActiveContractsByEmployee", Long.class)
                    .setParameter("employeeId", employeeId)
                    .setParameter("active", ContractStatus.ACTIVE).getSingleResult();
            if (activeCount > 0) {
                em.getTransaction().rollback();
                return Result.fail(error("Cet employe possede deja un contrat actif."));
            }
            contract.setEmployee(em.getReference(Employee.class, employeeId));
            em.persist(contract);
            em.getTransaction().commit();
            return Result.ok(contract);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while creating contract", ex);
            return Result.fail(error("Impossible de creer le contrat."));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> close(Integer contractId, LocalDate endDate) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            Contract contract = em.find(Contract.class, contractId);
            if (contract == null || contract.getStatus() != ContractStatus.ACTIVE) {
                em.getTransaction().rollback();
                return Result.fail(error("Seul un contrat actif peut etre cloture."));
            }
            contract.setEndDate(endDate);
            contract.setStatus(ContractStatus.CLOSED);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while closing contract", ex);
            return Result.fail(error("Impossible de cloturer le contrat."));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> renew(Integer contractId, LocalDate newEndDate) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            Contract contract = em.find(Contract.class, contractId);
            if (contract == null || contract.getStatus() != ContractStatus.ACTIVE) {
                em.getTransaction().rollback();
                return Result.fail(error("Seul un contrat actif peut etre renouvele."));
            }
            contract.setEndDate(newEndDate);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while renewing contract", ex);
            return Result.fail(error("Impossible de renouveler le contrat."));
        } finally { em.close(); }
    }

    @Override
    public Result<Void> updateSalary(Integer contractId, BigDecimal newSalary) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            Contract contract = em.find(Contract.class, contractId);
            if (contract == null || contract.getStatus() != ContractStatus.ACTIVE) {
                em.getTransaction().rollback();
                return Result.fail(error("Seul le salaire d'un contrat actif peut etre modifie."));
            }
            contract.setGrossSalary(newSalary);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while updating salary", ex);
            return Result.fail(error("Impossible de modifier le salaire."));
        } finally { em.close(); }
    }

    private Map<String, String> error(String message) {
        Map<String, String> errors = new HashMap<>();
        errors.put("message", message);
        return errors;
    }
}