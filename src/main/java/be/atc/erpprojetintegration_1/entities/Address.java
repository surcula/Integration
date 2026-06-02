package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "addresses")
public class Address {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 50)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "street_name", nullable = false, length = 50)
private java.lang.String streetName;

@javax.validation.constraints.Size(max = 11)
@javax.persistence.Column(name = "street_number", length = 11)
private java.lang.String streetNumber;

@javax.validation.constraints.Size(max = 5)
@javax.persistence.Column(name = "box_number", length = 5)
private java.lang.String boxNumber;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "city_id", nullable = false)
private be.atc.erpprojetintegration_1.City city;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getStreetName() {
  return streetName;
}public void setStreetName(java.lang.String streetName) {
  this.streetName = streetName;
}

public java.lang.String getStreetNumber() {
  return streetNumber;
}public void setStreetNumber(java.lang.String streetNumber) {
  this.streetNumber = streetNumber;
}

public java.lang.String getBoxNumber() {
  return boxNumber;
}public void setBoxNumber(java.lang.String boxNumber) {
  this.boxNumber = boxNumber;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.City getCity() {
  return city;
}public void setCity(be.atc.erpprojetintegration_1.City city) {
  this.city = city;
}

}