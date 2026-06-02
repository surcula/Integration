package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "training_centers")
public class TrainingCenter {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.persistence.Column(name = "training_career_name", length = 200)
private java.lang.String trainingCareerName;

@javax.validation.constraints.Size(max = 20)
@javax.persistence.Column(name = "phone", length = 20)
private java.lang.String phone;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "address_id")
private be.atc.erpprojetintegration_1.Address address;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getTrainingCareerName() {
  return trainingCareerName;
}public void setTrainingCareerName(java.lang.String trainingCareerName) {
  this.trainingCareerName = trainingCareerName;
}

public java.lang.String getPhone() {
  return phone;
}public void setPhone(java.lang.String phone) {
  this.phone = phone;
}

public be.atc.erpprojetintegration_1.Address getAddress() {
  return address;
}public void setAddress(be.atc.erpprojetintegration_1.Address address) {
  this.address = address;
}

}