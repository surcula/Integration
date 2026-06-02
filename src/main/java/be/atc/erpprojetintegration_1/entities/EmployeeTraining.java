package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "employee_trainings")
public class EmployeeTraining {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.persistence.Column(name = "obtained_date")
private java.time.LocalDate obtainedDate;

@javax.persistence.Column(name = "expiry_date")
private java.time.LocalDate expiryDate;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "status", length = 50)
private java.lang.String status;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "training_id", nullable = false)
private be.atc.erpprojetintegration_1.Training training;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "employee_id", nullable = false)
private be.atc.erpprojetintegration_1.Employee employee;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.time.LocalDate getObtainedDate() {
  return obtainedDate;
}public void setObtainedDate(java.time.LocalDate obtainedDate) {
  this.obtainedDate = obtainedDate;
}

public java.time.LocalDate getExpiryDate() {
  return expiryDate;
}public void setExpiryDate(java.time.LocalDate expiryDate) {
  this.expiryDate = expiryDate;
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

public be.atc.erpprojetintegration_1.Training getTraining() {
  return training;
}public void setTraining(be.atc.erpprojetintegration_1.Training training) {
  this.training = training;
}

public be.atc.erpprojetintegration_1.Employee getEmployee() {
  return employee;
}public void setEmployee(be.atc.erpprojetintegration_1.Employee employee) {
  this.employee = employee;
}

}