package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "objectives")
public class Objective {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "objectives_name", nullable = false, length = 200)
private java.lang.String objectivesName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getObjectivesName() {
  return objectivesName;
}public void setObjectivesName(java.lang.String objectivesName) {
  this.objectivesName = objectivesName;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public java.lang.String getDescription() {
  return description;
}public void setDescription(java.lang.String description) {
  this.description = description;
}

}