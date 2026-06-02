package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "cities")
public class City {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 100)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "city_name", nullable = false, length = 100)
private java.lang.String cityName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "zip_code", nullable = false)
private java.lang.Integer zipCode;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getCityName() {
  return cityName;
}public void setCityName(java.lang.String cityName) {
  this.cityName = cityName;
}

public java.lang.Integer getZipCode() {
  return zipCode;
}public void setZipCode(java.lang.Integer zipCode) {
  this.zipCode = zipCode;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

}