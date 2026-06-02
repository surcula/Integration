package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "superiors")
public class Superior {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "superior_name", nullable = false, length = 200)
private java.lang.String superiorName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.Column(name = "start_date")
private java.time.LocalDate startDate;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "employee_id", nullable = false)
private be.atc.erpprojetintegration_1.Employee employee;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getSuperiorName() {
  return superiorName;
}public void setSuperiorName(java.lang.String superiorName) {
  this.superiorName = superiorName;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public java.time.LocalDate getStartDate() {
  return startDate;
}public void setStartDate(java.time.LocalDate startDate) {
  this.startDate = startDate;
}

public be.atc.erpprojetintegration_1.Employee getEmployee() {
  return employee;
}public void setEmployee(be.atc.erpprojetintegration_1.Employee employee) {
  this.employee = employee;
}

}