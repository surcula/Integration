package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "candidates")
public class Candidate {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 100)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "first_name", nullable = false, length = 100)
private java.lang.String firstName;

@javax.validation.constraints.Size(max = 100)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "last_name", nullable = false, length = 100)
private java.lang.String lastName;

@javax.persistence.Column(name = "birth_date")
private java.time.LocalDate birthDate;

@javax.validation.constraints.Size(max = 150)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "email", nullable = false, length = 150)
private java.lang.String email;

@javax.validation.constraints.Size(max = 20)
@javax.persistence.Column(name = "phone", length = 20)
private java.lang.String phone;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "status", length = 50)
private java.lang.String status;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getFirstName() {
  return firstName;
}public void setFirstName(java.lang.String firstName) {
  this.firstName = firstName;
}

public java.lang.String getLastName() {
  return lastName;
}public void setLastName(java.lang.String lastName) {
  this.lastName = lastName;
}

public java.time.LocalDate getBirthDate() {
  return birthDate;
}public void setBirthDate(java.time.LocalDate birthDate) {
  this.birthDate = birthDate;
}

public java.lang.String getEmail() {
  return email;
}public void setEmail(java.lang.String email) {
  this.email = email;
}

public java.lang.String getPhone() {
  return phone;
}public void setPhone(java.lang.String phone) {
  this.phone = phone;
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

}