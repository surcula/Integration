package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "trainings")
public class Training {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.persistence.Column(name = "training_certificate_name", length = 200)
private java.lang.String trainingCertificateName;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "code", length = 50)
private java.lang.String code;

@javax.persistence.Column(name = "validity_months")
private java.lang.Integer validityMonths;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getTrainingCertificateName() {
  return trainingCertificateName;
}public void setTrainingCertificateName(java.lang.String trainingCertificateName) {
  this.trainingCertificateName = trainingCertificateName;
}

public java.lang.String getCode() {
  return code;
}public void setCode(java.lang.String code) {
  this.code = code;
}

public java.lang.Integer getValidityMonths() {
  return validityMonths;
}public void setValidityMonths(java.lang.Integer validityMonths) {
  this.validityMonths = validityMonths;
}

public java.lang.String getDescription() {
  return description;
}public void setDescription(java.lang.String description) {
  this.description = description;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

}