package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "evaluations_objectives")
public class EvaluationsObjective {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.persistence.Column(name = "evaluation_objectives_name", length = 200)
private java.lang.String evaluationObjectivesName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "objective_id", nullable = false)
private be.atc.erpprojetintegration_1.Objective objective;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "evaluation_id", nullable = false)
private be.atc.erpprojetintegration_1.Evaluation evaluation;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getEvaluationObjectivesName() {
  return evaluationObjectivesName;
}public void setEvaluationObjectivesName(java.lang.String evaluationObjectivesName) {
  this.evaluationObjectivesName = evaluationObjectivesName;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Objective getObjective() {
  return objective;
}public void setObjective(be.atc.erpprojetintegration_1.Objective objective) {
  this.objective = objective;
}

public be.atc.erpprojetintegration_1.Evaluation getEvaluation() {
  return evaluation;
}public void setEvaluation(be.atc.erpprojetintegration_1.Evaluation evaluation) {
  this.evaluation = evaluation;
}

}