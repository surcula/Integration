package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "criteria")
public class Criterion {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "criterion_name", nullable = false, length = 200)
private java.lang.String criterionName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "category_id")
private be.atc.erpprojetintegration_1.Category category;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "function_criterion_id")
private be.atc.erpprojetintegration_1.CriteriaFunction functionCriterion;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getCriterionName() {
  return criterionName;
}public void setCriterionName(java.lang.String criterionName) {
  this.criterionName = criterionName;
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

public be.atc.erpprojetintegration_1.Category getCategory() {
  return category;
}public void setCategory(be.atc.erpprojetintegration_1.Category category) {
  this.category = category;
}

public be.atc.erpprojetintegration_1.CriteriaFunction getFunctionCriterion() {
  return functionCriterion;
}public void setFunctionCriterion(be.atc.erpprojetintegration_1.CriteriaFunction functionCriterion) {
  this.functionCriterion = functionCriterion;
}

}