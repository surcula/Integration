package be.atc.erpprojetintegration_1.controllers;

import be.atc.erpprojetintegration_1.business.ContractBusiness;
import be.atc.erpprojetintegration_1.business.EmployeeBusiness;
import be.atc.erpprojetintegration_1.dto.EmployeeListDto;
import be.atc.erpprojetintegration_1.entities.Contract;
import be.atc.erpprojetintegration_1.enums.ContractStatus;
import be.atc.erpprojetintegration_1.enums.ContractType;
import be.atc.erpprojetintegration_1.tools.Result;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class ContractsBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject private ContractBusiness contractBusiness;
    @Inject private EmployeeBusiness employeeBusiness;
    @Inject private AuthBean authBean;

    private List<Contract> contracts;
    private List<EmployeeListDto> employees;
    private Contract selectedContract;
    private Integer selectedEmployeeId;
    private LocalDate closeDate;
    private LocalDate renewalEndDate;
    private BigDecimal newSalary;

    @PostConstruct
    public void init() {
        load();
        prepareNew();
    }

    public void load() {
        if (!isCanAccess()) {
            contracts = new ArrayList<>();
            employees = new ArrayList<>();
            return;
        }
        Result<List<Contract>> result = contractBusiness.getAccessible(
                connectedId(), isCanManage(), isEmployee());
        contracts = result.isSuccess() ? result.getData() : new ArrayList<Contract>();
        employees = new ArrayList<>();
        if (isCanManage()) {
            Result<List<EmployeeListDto>> employeeResult = employeeBusiness.getEmployeeList(false);
            if (employeeResult.isSuccess()) employees = employeeResult.getData();
        }
    }

    public void prepareNew() {
        selectedContract = new Contract();
        selectedContract.setStartDate(LocalDate.now());
        selectedContract.setContractType(ContractType.PERMANENT);
        selectedContract.setStatus(ContractStatus.ACTIVE);
        selectedContract.setIsActive(true);
        selectedEmployeeId = null;
    }

    public void create() {
        Result<Contract> result = contractBusiness.create(selectedContract, selectedEmployeeId, isCanManage());
        if (!result.isSuccess()) {
            fail(result);
            return;
        }
        message(FacesMessage.SEVERITY_INFO, "Contrat cree.");
        load();
        prepareNew();
    }

    public void prepareClose(Contract contract) {
        selectedContract = contract;
        closeDate = LocalDate.now();
    }

    public void close() {
        Result<Void> result = contractBusiness.close(selectedContract.getId(), closeDate, isCanManage());
        if (!result.isSuccess()) {
            fail(result);
            return;
        }
        message(FacesMessage.SEVERITY_INFO, "Contrat cloture.");
        load();
    }

    public void prepareRenewal(Contract contract) {
        selectedContract = contract;
        renewalEndDate = contract.getEndDate();
    }

    public void renew() {
        Result<Void> result = contractBusiness.renew(
                selectedContract.getId(), renewalEndDate, isCanManage());
        if (!result.isSuccess()) {
            fail(result);
            return;
        }
        message(FacesMessage.SEVERITY_INFO, "Date de fin prolongee.");
        load();
    }

    public void prepareSalary(Contract contract) {
        selectedContract = contract;
        newSalary = contract.getGrossSalary();
    }

    public void updateSalary() {
        Result<Void> result = contractBusiness.updateSalary(
                selectedContract.getId(), newSalary, isCanManage());
        if (!result.isSuccess()) {
            fail(result);
            return;
        }
        message(FacesMessage.SEVERITY_INFO, "Salaire brut mis a jour.");
        load();
    }

    private void fail(Result<?> result) {
        message(FacesMessage.SEVERITY_ERROR, firstError(result));
        FacesContext.getCurrentInstance().validationFailed();
    }

    private Integer connectedId() {
        return authBean.getConnectedEmployee() == null ? null : authBean.getConnectedEmployee().getId();
    }

    private String firstError(Result<?> result) {
        return result.getErrors() == null || result.getErrors().isEmpty()
                ? "Operation impossible."
                : result.getErrors().values().iterator().next();
    }

    private void message(FacesMessage.Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, text, null));
    }

    public boolean isCanManage() {
        return authBean.hasRole("HR") || authBean.hasRole("ADMIN");
    }

    public boolean isEmployee() {
        return authBean.hasRole("EMPLOYEE");
    }

    public boolean isCanAccess() {
        return isCanManage() || isEmployee();
    }

    public boolean canClose(Contract contract) {
        return isCanManage() && contract != null && contract.getStatus() == ContractStatus.ACTIVE;
    }

    public boolean canRenew(Contract contract) {
        return canClose(contract) && contract.getContractType() != ContractType.PERMANENT;
    }

    public boolean canUpdateSalary(Contract contract) {
        return canClose(contract);
    }

    public String statusClass(Contract contract) {
        return "contract-status contract-status-" + contract.getStatus().name().toLowerCase();
    }

    public long getActiveCount() {
        return contracts.stream().filter(c -> c.getStatus() == ContractStatus.ACTIVE).count();
    }

    public List<Contract> getContracts() { return contracts; }
    public List<EmployeeListDto> getEmployees() { return employees; }
    public Contract getSelectedContract() { return selectedContract; }
    public void setSelectedContract(Contract selectedContract) { this.selectedContract = selectedContract; }
    public Integer getSelectedEmployeeId() { return selectedEmployeeId; }
    public void setSelectedEmployeeId(Integer selectedEmployeeId) { this.selectedEmployeeId = selectedEmployeeId; }
    public LocalDate getCloseDate() { return closeDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public LocalDate getRenewalEndDate() { return renewalEndDate; }
    public void setRenewalEndDate(LocalDate renewalEndDate) { this.renewalEndDate = renewalEndDate; }
    public BigDecimal getNewSalary() { return newSalary; }
    public void setNewSalary(BigDecimal newSalary) { this.newSalary = newSalary; }
    public ContractType[] getContractTypes() { return ContractType.values(); }
}