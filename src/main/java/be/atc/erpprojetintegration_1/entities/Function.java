package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "functions")
public class Function {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 150)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "function_name", nullable = false, length = 150)
private java.lang.String functionName;

@javax.persistence.Column(name = "mandatory")
private java.lang.Boolean mandatory;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

@javax.persistence.Column(name = "number_of_open_positions")
private java.lang.Integer numberOfOpenPositions;

@javax.persistence.Lob
@javax.persistence.Column(name = "job_description")
private java.lang.String jobDescription;

@javax.persistence.Lob
@javax.persistence.Column(name = "comments")
private java.lang.String comments;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "status", length = 50)
private java.lang.String status;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "function_id")
private be.atc.erpprojetintegration_1.Function function;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "city_id")
private be.atc.erpprojetintegration_1.City city;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getFunctionName() {
  return functionName;
}public void setFunctionName(java.lang.String functionName) {
  this.functionName = functionName;
}

public java.lang.Boolean getMandatory() {
  return mandatory;
}public void setMandatory(java.lang.Boolean mandatory) {
  this.mandatory = mandatory;
}

public java.lang.String getDescription() {
  return description;
}public void setDescription(java.lang.String description) {
  this.description = description;
}

public java.lang.Integer getNumberOfOpenPositions() {
  return numberOfOpenPositions;
}public void setNumberOfOpenPositions(java.lang.Integer numberOfOpenPositions) {
  this.numberOfOpenPositions = numberOfOpenPositions;
}

public java.lang.String getJobDescription() {
  return jobDescription;
}public void setJobDescription(java.lang.String jobDescription) {
  this.jobDescription = jobDescription;
}

public java.lang.String getComments() {
  return comments;
}public void setComments(java.lang.String comments) {
  this.comments = comments;
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

public be.atc.erpprojetintegration_1.Function getFunction() {
  return function;
}public void setFunction(be.atc.erpprojetintegration_1.Function function) {
  this.function = function;
}

public be.atc.erpprojetintegration_1.City getCity() {
  return city;
}public void setCity(be.atc.erpprojetintegration_1.City city) {
  this.city = city;
}

}