package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "evaluations")
public class Evaluation {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "evaluation_name", nullable = false, length = 200)
private java.lang.String evaluationName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.Column(name = "evaluation_date")
private java.time.LocalDate evaluationDate;

@javax.persistence.Column(name = "period_start")
private java.time.LocalDate periodStart;

@javax.persistence.Column(name = "period_end")
private java.time.LocalDate periodEnd;

@javax.persistence.Column(name = "global_score", precision = 10, scale = 2)
private java.math.BigDecimal globalScore;

@javax.persistence.Lob
@javax.persistence.Column(name = "comments")
private java.lang.String comments;

@javax.persistence.Column(name = "self_global_score", precision = 10, scale = 2)
private java.math.BigDecimal selfGlobalScore;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "evaluator_id", nullable = false)
private be.atc.erpprojetintegration_1.Employee evaluator;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "employee_id", nullable = false)
private be.atc.erpprojetintegration_1.Employee employee;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getEvaluationName() {
  return evaluationName;
}public void setEvaluationName(java.lang.String evaluationName) {
  this.evaluationName = evaluationName;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public java.time.LocalDate getEvaluationDate() {
  return evaluationDate;
}public void setEvaluationDate(java.time.LocalDate evaluationDate) {
  this.evaluationDate = evaluationDate;
}

public java.time.LocalDate getPeriodStart() {
  return periodStart;
}public void setPeriodStart(java.time.LocalDate periodStart) {
  this.periodStart = periodStart;
}

public java.time.LocalDate getPeriodEnd() {
  return periodEnd;
}public void setPeriodEnd(java.time.LocalDate periodEnd) {
  this.periodEnd = periodEnd;
}

public java.math.BigDecimal getGlobalScore() {
  return globalScore;
}public void setGlobalScore(java.math.BigDecimal globalScore) {
  this.globalScore = globalScore;
}

public java.lang.String getComments() {
  return comments;
}public void setComments(java.lang.String comments) {
  this.comments = comments;
}

public java.math.BigDecimal getSelfGlobalScore() {
  return selfGlobalScore;
}public void setSelfGlobalScore(java.math.BigDecimal selfGlobalScore) {
  this.selfGlobalScore = selfGlobalScore;
}

public be.atc.erpprojetintegration_1.Employee getEvaluator() {
  return evaluator;
}public void setEvaluator(be.atc.erpprojetintegration_1.Employee evaluator) {
  this.evaluator = evaluator;
}

public be.atc.erpprojetintegration_1.Employee getEmployee() {
  return employee;
}public void setEmployee(be.atc.erpprojetintegration_1.Employee employee) {
  this.employee = employee;
}

}