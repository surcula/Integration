package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.ContractStatus;
import be.atc.erpprojetintegration_1.enums.ContractType;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.math.BigDecimal;

@NamedQueries({
        @NamedQuery(name = "getAllContracts", query = "SELECT c FROM Contract c JOIN FETCH c.employee WHERE c.isActive = true ORDER BY c.employee.lastName, c.employee.firstName, c.startDate DESC"),
        @NamedQuery(name = "getContractsByEmployee", query = "SELECT c FROM Contract c JOIN FETCH c.employee WHERE c.isActive = true AND c.employee.id = :employeeId ORDER BY c.startDate DESC"),
        @NamedQuery(name = "getActiveContractByEmployee", query = "SELECT c FROM Contract c JOIN FETCH c.employee WHERE c.isActive = true AND c.employee.id = :employeeId AND c.status = :active"),
        @NamedQuery(name = "getContractById", query = "SELECT c FROM Contract c JOIN FETCH c.employee WHERE c.id = :contractId AND c.isActive = true"),
        @NamedQuery(name = "countActiveContractsByEmployee", query = "SELECT COUNT(c) FROM Contract c WHERE c.employee.id = :employeeId AND c.isActive = true AND c.status = :active")
})
@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType contractType;

    @NotNull
    @Column(name = "gross_salary", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossSalary;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ContractStatus status;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public BigDecimal getGrossSalary() {
        return grossSalary;
    }

    public void setGrossSalary(BigDecimal grossSalary) {
        this.grossSalary = grossSalary;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public void setStatus(ContractStatus status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

}
