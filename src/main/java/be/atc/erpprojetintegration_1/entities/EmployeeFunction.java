package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "employee_functions")
public class EmployeeFunction {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.persistence.Column(name = "start_date")
private java.time.LocalDate startDate;

@javax.persistence.Column(name = "end_date")
private java.time.LocalDate endDate;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "function_id", nullable = false)
private be.atc.erpprojetintegration_1.Function function;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
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

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Function getFunction() {
  return function;
}public void setFunction(be.atc.erpprojetintegration_1.Function function) {
  this.function = function;
}

}