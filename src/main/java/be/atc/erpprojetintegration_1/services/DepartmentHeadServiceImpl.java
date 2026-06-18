package be.atc.erpprojetintegration_1.services;

import be.atc.erpprojetintegration_1.entities.Department;
import be.atc.erpprojetintegration_1.entities.DepartmentHead;
import be.atc.erpprojetintegration_1.entities.Employee;
import be.atc.erpprojetintegration_1.interfaces.IDepartmentHeadService;
import be.atc.erpprojetintegration_1.tools.EMF;
import be.atc.erpprojetintegration_1.tools.Result;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DepartmentHeadServiceImpl implements IDepartmentHeadService {
    private static final Logger log = Logger.getLogger(DepartmentHeadServiceImpl.class);

    @Override
    public Result<List<DepartmentHead>> getAllActive() {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getAllActiveDepartmentHeads", DepartmentHead.class).getResultList());
        } catch (Exception ex) {
            log.error("Error while searching active department heads", ex);
            return Result.fail(error("message", "superiors.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<DepartmentHead>> getAll() {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getAllDepartmentHeads", DepartmentHead.class).getResultList());
        } catch (Exception ex) {
            log.error("Error while loading department heads", ex);
            return Result.fail(error("message", "Impossible de charger les chefs de departement."));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> getById(Integer id) {
        EntityManager em = EMF.getEM();
        try {
            List<DepartmentHead> rows = em.createNamedQuery("getDepartmentHeadById", DepartmentHead.class)
                    .setParameter("id", id).getResultList();
            return rows.isEmpty() ? Result.fail(error("notFound", "Chef de departement introuvable.")) : Result.ok(rows.get(0));
        } catch (Exception ex) {
            log.error("Error while loading department head", ex);
            return Result.fail(error("message", "Impossible de charger le chef de departement."));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> getActiveBySuperiorAndDepartment(Integer superiorId, Integer departmentId) {
        EntityManager em = EMF.getEM();
        try {
            DepartmentHead departmentHead = em.createNamedQuery("getActiveDepartmentHeadBySuperiorAndDepartment", DepartmentHead.class)
                    .setParameter("superiorId", superiorId)
                    .setParameter("departmentId", departmentId)
                    .getSingleResult();
            return Result.ok(departmentHead);
        } catch (NoResultException ex) {
            return Result.fail(error("notFound", "superiors.departmentHead.notFound"));
        } catch (Exception ex) {
            log.error("Error while searching active department head", ex);
            return Result.fail(error("message", "superiors.error.load"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> getActiveByDepartmentId(Integer departmentId) {
        EntityManager em = EMF.getEM();
        try {
            List<DepartmentHead> rows = em.createNamedQuery("getActiveDepartmentHeadByDepartmentId", DepartmentHead.class)
                    .setParameter("departmentId", departmentId).getResultList();
            return rows.isEmpty() ? Result.fail(error("notFound", "Aucun chef actif.")) : Result.ok(rows.get(0));
        } catch (Exception ex) {
            log.error("Error while loading active department head", ex);
            return Result.fail(error("message", "Impossible de charger le chef actif."));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<List<DepartmentHead>> getActiveByEmployeeId(Integer employeeId) {
        EntityManager em = EMF.getEM();
        try {
            return Result.ok(em.createNamedQuery("getActiveDepartmentHeadsByEmployeeId", DepartmentHead.class)
                    .setParameter("employeeId", employeeId).getResultList());
        } catch (Exception ex) {
            log.error("Error while loading department heads for employee", ex);
            return Result.fail(error("message", "Impossible de charger les departements geres."));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> create(DepartmentHead departmentHead) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            departmentHead.setDepartment(em.getReference(Department.class, departmentHead.getDepartment().getId()));
            departmentHead.setSuperior(em.getReference(Employee.class, departmentHead.getSuperior().getId()));
            em.persist(departmentHead);
            em.getTransaction().commit();
            return Result.ok(departmentHead);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while creating department head", ex);
            return Result.fail(error("message", "superiors.departmentHead.save.error"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<DepartmentHead> assign(DepartmentHead departmentHead) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("closeActiveDepartmentHeads")
                    .setParameter("departmentId", departmentHead.getDepartment().getId())
                    .setParameter("endDate", departmentHead.getStartDate().minusDays(1))
                    .executeUpdate();
            departmentHead.setDepartment(em.getReference(Department.class, departmentHead.getDepartment().getId()));
            departmentHead.setSuperior(em.getReference(Employee.class, departmentHead.getSuperior().getId()));
            departmentHead.setIsActive(true);
            departmentHead.setEndDate(null);
            em.persist(departmentHead);
            em.getTransaction().commit();
            return Result.ok(departmentHead);
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while assigning department head", ex);
            return Result.fail(error("message", "Impossible d'enregistrer le chef de departement."));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> setActive(Integer id, boolean active) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            DepartmentHead departmentHead = em.find(DepartmentHead.class, id);
            if (departmentHead == null) {
                em.getTransaction().rollback();
                return Result.fail(error("notFound", "superiors.departmentHead.notFound"));
            }
            departmentHead.setIsActive(active);
            departmentHead.setEndDate(active ? null : LocalDate.now());
            if (active && departmentHead.getStartDate() == null) {
                departmentHead.setStartDate(LocalDate.now());
            }
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while changing department head active status", ex);
            return Result.fail(error("message", active ? "superiors.activate.error" : "superiors.delete.error"));
        } finally {
            em.close();
        }
    }

    @Override
    public Result<Void> deactivate(Integer id) {
        return deactivate(id, LocalDate.now());
    }

    @Override
    public Result<Void> deactivate(Integer id, LocalDate endDate) {
        EntityManager em = EMF.getEM();
        try {
            em.getTransaction().begin();
            DepartmentHead departmentHead = em.find(DepartmentHead.class, id);
            if (departmentHead == null) {
                em.getTransaction().rollback();
                return Result.fail(error("notFound", "Chef de departement introuvable."));
            }
            departmentHead.setEndDate(endDate);
            departmentHead.setIsActive(false);
            em.getTransaction().commit();
            return Result.ok();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            log.error("Error while deactivating department head", ex);
            return Result.fail(error("message", "Impossible de terminer le mandat."));
        } finally {
            em.close();
        }
    }

    private Map<String, String> error(String key, String value) {
        Map<String, String> errors = new HashMap<>();
        errors.put(key, value);
        return errors;
    }
}
