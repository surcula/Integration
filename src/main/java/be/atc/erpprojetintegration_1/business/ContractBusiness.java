package be.atc.erpprojetintegration_1.business;

import be.atc.erpprojetintegration_1.entities.Contract;
import be.atc.erpprojetintegration_1.enums.ContractStatus;
import be.atc.erpprojetintegration_1.enums.ContractType;
import be.atc.erpprojetintegration_1.interfaces.IContractService;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ContractBusiness {
    @Inject private IContractService contractService;

    public Result<List<Contract>> getAccessible(Integer employeeId, boolean canEditContracts, boolean employee) {
        if (canEditContracts) return contractService.getAll();
        if (employee && employeeId != null) return contractService.getByEmployee(employeeId);
        return Result.fail(error("access", "Acces refuse."));
    }

    public Result<Contract> getActive(Integer employeeId, Integer connectedEmployeeId, boolean canEditContracts) {
        if (!canEditContracts && (connectedEmployeeId == null || !connectedEmployeeId.equals(employeeId))) {
            return Result.fail(error("access", "Acces refuse."));
        }
        if (employeeId == null) return Result.fail(error("employee", "L'employe est obligatoire."));
        return contractService.getActive(employeeId);
    }

    public Result<List<Contract>> getByEmployee(Integer employeeId, Integer connectedEmployeeId, boolean canEditContracts) {
        if (!canEditContracts && (connectedEmployeeId == null || !connectedEmployeeId.equals(employeeId))) {
            return Result.fail(error("access", "Acces refuse."));
        }
        if (employeeId == null) return Result.fail(error("employee", "L'employe est obligatoire."));
        return contractService.getByEmployee(employeeId);
    }

    public Result<Contract> create(Contract contract, Integer employeeId, boolean canEditContracts) {
        if (!canEditContracts) return Result.fail(error("access", "Seule la RH peut creer un contrat."));
        Result<Void> validation = validateContract(contract, employeeId);
        if (!validation.isSuccess()) return Result.fail(validation.getErrors());
        contract.setStatus(ContractStatus.ACTIVE);
        contract.setIsActive(true);
        if (contract.getContractType() == ContractType.PERMANENT) contract.setEndDate(null);
        return contractService.create(contract, employeeId);
    }

    public Result<Void> close(Integer contractId, LocalDate endDate, boolean canEditContracts) {
        if (!canEditContracts) return Result.fail(error("access", "Seule la RH peut cloturer un contrat."));
        Map<String, String> errors = new HashMap<>();
        if (contractId == null) errors.put("contract", "Le contrat est obligatoire.");
        if (endDate == null) errors.put("endDate", "La date de fin est obligatoire.");
        if (!errors.isEmpty()) return Result.fail(errors);

        Result<Contract> contract = contractService.getById(contractId);
        if (!contract.isSuccess()) return Result.fail(contract.getErrors());
        if (contract.getData() == null || contract.getData().getStatus() != ContractStatus.ACTIVE) {
            return Result.fail(error("status", "Seul un contrat actif peut etre cloture."));
        }
        if (contract.getData().getStartDate() != null && endDate.isBefore(contract.getData().getStartDate())) {
            return Result.fail(error("endDate", "La date de fin ne peut pas preceder la date de debut."));
        }
        return contractService.close(contractId, endDate);
    }

    public Result<Void> renew(Integer contractId, LocalDate newEndDate, boolean canEditContracts) {
        if (!canEditContracts) return Result.fail(error("access", "Seule la RH peut renouveler un contrat."));
        if (contractId == null) return Result.fail(error("contract", "Le contrat est obligatoire."));
        if (newEndDate == null) return Result.fail(error("endDate", "La nouvelle date de fin est obligatoire."));

        Result<Contract> contract = contractService.getById(contractId);
        if (!contract.isSuccess()) return Result.fail(contract.getErrors());
        Contract data = contract.getData();
        if (data == null || data.getStatus() != ContractStatus.ACTIVE) {
            return Result.fail(error("status", "Seul un contrat actif peut etre renouvele."));
        }
        if (data.getContractType() == ContractType.PERMANENT) {
            return Result.fail(error("type", "Un CDI ne peut pas etre renouvele."));
        }
        if (data.getStartDate() != null && newEndDate.isBefore(data.getStartDate())) {
            return Result.fail(error("endDate", "La date de fin doit suivre la date de debut."));
        }
        if (data.getEndDate() != null && newEndDate.isBefore(data.getEndDate())) {
            return Result.fail(error("endDate", "La nouvelle date doit prolonger le contrat."));
        }
        return contractService.renew(contractId, newEndDate);
    }

    public Result<Void> updateSalary(Integer contractId, BigDecimal newSalary, boolean canEditContracts) {
        if (!canEditContracts) return Result.fail(error("access", "Seule la RH peut modifier le salaire."));
        if (contractId == null) return Result.fail(error("contract", "Le contrat est obligatoire."));
        if (newSalary == null || newSalary.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.fail(error("salary", "Le salaire brut doit etre positif."));
        }
        return contractService.updateSalary(contractId, newSalary);
    }

    private Result<Void> validateContract(Contract contract, Integer employeeId) {
        Map<String, String> errors = new HashMap<>();
        if (contract == null) return Result.fail(error("contract", "Formulaire invalide."));
        if (employeeId == null) errors.put("employee", "L'employe est obligatoire.");
        if (contract.getStartDate() == null) errors.put("startDate", "La date de debut est obligatoire.");
        if (contract.getContractType() == null) errors.put("type", "Le type de contrat est obligatoire.");
        if (contract.getGrossSalary() == null || contract.getGrossSalary().compareTo(BigDecimal.ZERO) <= 0) {
            errors.put("salary", "Le salaire brut doit etre positif.");
        }
        if (contract.getContractType() != ContractType.PERMANENT && contract.getEndDate() == null) {
            errors.put("endDate", "La date de fin est obligatoire pour ce type de contrat.");
        }
        if (contract.getStartDate() != null && contract.getEndDate() != null
                && contract.getEndDate().isBefore(contract.getStartDate())) {
            errors.put("dates", "La date de fin doit suivre la date de debut.");
        }
        return errors.isEmpty() ? Result.ok() : Result.fail(errors);
    }

    private Map<String, String> error(String key, String value) {
        Map<String, String> errors = new HashMap<>();
        errors.put(key, value);
        return errors;
    }
}
