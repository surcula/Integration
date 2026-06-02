package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "contracts")
public class Contract {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 50)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "contract_code", nullable = false, length = 50)
private java.lang.String contractCode;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "start_date", nullable = false)
private java.time.LocalDate startDate;

@javax.persistence.Column(name = "end_date")
private java.time.LocalDate endDate;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "status", length = 50)
private java.lang.String status;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "employee_id", nullable = false)
private be.atc.erpprojetintegration_1.Employee employee;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getContractCode() {
  return contractCode;
}public void setContractCode(java.lang.String contractCode) {
  this.contractCode = contractCode;
}

public java.time.LocalDate getStartDate() {
  return startDate;
}public void setStartDate(java.time.LocalDate startDate) {
  this.startDate = startDate;
}

public java.time.LocalDate getEndDate() {
  return endDate;
}public void setEndDate(java.time.LocalDate endDate) {
  this.endDate = endDate;
}

public java.lang.String getStatus() {
  return status;
}public void setStatus(java.lang.String status) {
  this.status = status;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Employee getEmployee() {
  return employee;
}public void setEmployee(be.atc.erpprojetintegration_1.Employee employee) {
  this.employee = employee;
}

}