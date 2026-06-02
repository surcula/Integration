package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "training_functions")
public class TrainingFunction {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.persistence.Column(name = "mandatory")
private java.lang.Boolean mandatory;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "training_id", nullable = false)
private be.atc.erpprojetintegration_1.Training training;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "function_id", nullable = false)
private be.atc.erpprojetintegration_1.Function function;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.Boolean getMandatory() {
  return mandatory;
}public void setMandatory(java.lang.Boolean mandatory) {
  this.mandatory = mandatory;
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

public be.atc.erpprojetintegration_1.Function getFunction() {
  return function;
}public void setFunction(be.atc.erpprojetintegration_1.Function function) {
  this.function = function;
}

}