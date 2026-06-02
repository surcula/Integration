package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "plannings_departments")
public class PlanningsDepartment {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "planning_id", nullable = false)
private be.atc.erpprojetintegration_1.Planning planning;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "department_id", nullable = false)
private be.atc.erpprojetintegration_1.Department department;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Planning getPlanning() {
  return planning;
}public void setPlanning(be.atc.erpprojetintegration_1.Planning planning) {
  this.planning = planning;
}

public be.atc.erpprojetintegration_1.Department getDepartment() {
  return department;
}public void setDepartment(be.atc.erpprojetintegration_1.Department department) {
  this.department = department;
}

}