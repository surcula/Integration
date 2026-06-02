package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "criteria_function")
public class CriteriaFunction {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "function_id", nullable = false)
private be.atc.erpprojetintegration_1.Function function;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "criterion_id", nullable = false)
private be.atc.erpprojetintegration_1.Criterion criterion;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public be.atc.erpprojetintegration_1.Function getFunction() {
  return function;
}public void setFunction(be.atc.erpprojetintegration_1.Function function) {
  this.function = function;
}

public be.atc.erpprojetintegration_1.Criterion getCriterion() {
  return criterion;
}public void setCriterion(be.atc.erpprojetintegration_1.Criterion criterion) {
  this.criterion = criterion;
}

}