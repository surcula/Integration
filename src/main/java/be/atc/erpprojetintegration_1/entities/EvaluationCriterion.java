package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "evaluation_criteria")
public class EvaluationCriterion {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.persistence.Column(name = "note", precision = 10, scale = 2)
private java.math.BigDecimal note;

@javax.persistence.Lob
@javax.persistence.Column(name = "comment")
private java.lang.String comment;

@javax.persistence.Column(name = "self_evaluation", precision = 10, scale = 2)
private java.math.BigDecimal selfEvaluation;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "evaluation_id", nullable = false)
private be.atc.erpprojetintegration_1.Evaluation evaluation;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "criterion_id", nullable = false)
private be.atc.erpprojetintegration_1.Criterion criterion;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.math.BigDecimal getNote() {
  return note;
}public void setNote(java.math.BigDecimal note) {
  this.note = note;
}

public java.lang.String getComment() {
  return comment;
}public void setComment(java.lang.String comment) {
  this.comment = comment;
}

public java.math.BigDecimal getSelfEvaluation() {
  return selfEvaluation;
}public void setSelfEvaluation(java.math.BigDecimal selfEvaluation) {
  this.selfEvaluation = selfEvaluation;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Evaluation getEvaluation() {
  return evaluation;
}public void setEvaluation(be.atc.erpprojetintegration_1.Evaluation evaluation) {
  this.evaluation = evaluation;
}

public be.atc.erpprojetintegration_1.Criterion getCriterion() {
  return criterion;
}public void setCriterion(be.atc.erpprojetintegration_1.Criterion criterion) {
  this.criterion = criterion;
}

}