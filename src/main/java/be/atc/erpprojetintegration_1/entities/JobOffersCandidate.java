package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "job_offers_candidates")
public class JobOffersCandidate {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.persistence.Column(name = "job_offer_name", length = 200)
private java.lang.String jobOfferName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "application_date", nullable = false)
private java.time.LocalDate applicationDate;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "application_status", length = 50)
private java.lang.String applicationStatus;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "job_offers_id", nullable = false)
private be.atc.erpprojetintegration_1.JobOffer jobOffers;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "candidate_id", nullable = false)
private be.atc.erpprojetintegration_1.Candidate candidate;

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

public java.time.LocalDate getApplicationDate() {
  return applicationDate;
}public void setApplicationDate(java.time.LocalDate applicationDate) {
  this.applicationDate = applicationDate;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public java.lang.String getApplicationStatus() {
  return applicationStatus;
}public void setApplicationStatus(java.lang.String applicationStatus) {
  this.applicationStatus = applicationStatus;
}

public be.atc.erpprojetintegration_1.JobOffer getJobOffers() {
  return jobOffers;
}public void setJobOffers(be.atc.erpprojetintegration_1.JobOffer jobOffers) {
  this.jobOffers = jobOffers;
}

public be.atc.erpprojetintegration_1.Candidate getCandidate() {
  return candidate;
}public void setCandidate(be.atc.erpprojetintegration_1.Candidate candidate) {
  this.candidate = candidate;
}

}