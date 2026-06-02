package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "job_offers")
public class JobOffer {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "job_offer_name", nullable = false, length = 200)
private java.lang.String jobOfferName;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "create_at", nullable = false)
private java.time.Instant createAt;

@javax.persistence.Column(name = "publish_start_date")
private java.time.LocalDate publishStartDate;

@javax.persistence.Column(name = "publish_end_date")
private java.time.LocalDate publishEndDate;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "status", length = 50)
private java.lang.String status;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "function_id", nullable = false)
private be.atc.erpprojetintegration_1.Function function;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getJobOfferName() {
  return jobOfferName;
}public void setJobOfferName(java.lang.String jobOfferName) {
  this.jobOfferName = jobOfferName;
}

public java.lang.String getDescription() {
  return description;
}public void setDescription(java.lang.String description) {
  this.description = description;
}

public java.time.Instant getCreateAt() {
  return createAt;
}public void setCreateAt(java.time.Instant createAt) {
  this.createAt = createAt;
}

public java.time.LocalDate getPublishStartDate() {
  return publishStartDate;
}public void setPublishStartDate(java.time.LocalDate publishStartDate) {
  this.publishStartDate = publishStartDate;
}

public java.time.LocalDate getPublishEndDate() {
  return publishEndDate;
}public void setPublishEndDate(java.time.LocalDate publishEndDate) {
  this.publishEndDate = publishEndDate;
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

}