package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "employees")
public class Employee {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 100)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "last_name", nullable = false, length = 100)
private java.lang.String lastName;

@javax.validation.constraints.Size(max = 100)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "first_name", nullable = false, length = 100)
private java.lang.String firstName;

@javax.persistence.Column(name = "birth_date")
private java.time.LocalDate birthDate;

@javax.validation.constraints.Size(max = 150)
@javax.persistence.Column(name = "place_of_birth", length = 150)
private java.lang.String placeOfBirth;

@javax.validation.constraints.Size(max = 20)
@javax.persistence.Column(name = "phone", length = 20)
private java.lang.String phone;

@javax.validation.constraints.Size(max = 150)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "email", nullable = false, length = 150)
private java.lang.String email;

@javax.validation.constraints.Size(max = 255)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "password", nullable = false)
private java.lang.String password;

@javax.validation.constraints.Size(max = 10)
@javax.persistence.Column(name = "civilite", length = 10)
private java.lang.String civilite;

@javax.validation.constraints.Size(max = 10)
@javax.persistence.Column(name = "gender", length = 10)
private java.lang.String gender;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "employment_status", length = 50)
private java.lang.String employmentStatus;

@javax.validation.constraints.Size(max = 50)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "employee_number", nullable = false, length = 50)
private java.lang.String employeeNumber;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "address_id")
private be.atc.erpprojetintegration_1.Address address;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "role_id")
private be.atc.erpprojetintegration_1.Role role;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getLastName() {
  return lastName;
}public void setLastName(java.lang.String lastName) {
  this.lastName = lastName;
}

public java.lang.String getFirstName() {
  return firstName;
}public void setFirstName(java.lang.String firstName) {
  this.firstName = firstName;
}

public java.time.LocalDate getBirthDate() {
  return birthDate;
}public void setBirthDate(java.time.LocalDate birthDate) {
  this.birthDate = birthDate;
}

public java.lang.String getPlaceOfBirth() {
  return placeOfBirth;
}public void setPlaceOfBirth(java.lang.String placeOfBirth) {
  this.placeOfBirth = placeOfBirth;
}

public java.lang.String getPhone() {
  return phone;
}public void setPhone(java.lang.String phone) {
  this.phone = phone;
}

public java.lang.String getEmail() {
  return email;
}public void setEmail(java.lang.String email) {
  this.email = email;
}

public java.lang.String getPassword() {
  return password;
}public void setPassword(java.lang.String password) {
  this.password = password;
}

public java.lang.String getCivilite() {
  return civilite;
}public void setCivilite(java.lang.String civilite) {
  this.civilite = civilite;
}

public java.lang.String getGender() {
  return gender;
}public void setGender(java.lang.String gender) {
  this.gender = gender;
}

public java.lang.String getEmploymentStatus() {
  return employmentStatus;
}public void setEmploymentStatus(java.lang.String employmentStatus) {
  this.employmentStatus = employmentStatus;
}

public java.lang.String getEmployeeNumber() {
  return employeeNumber;
}public void setEmployeeNumber(java.lang.String employeeNumber) {
  this.employeeNumber = employeeNumber;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Address getAddress() {
  return address;
}public void setAddress(be.atc.erpprojetintegration_1.Address address) {
  this.address = address;
}

public be.atc.erpprojetintegration_1.Role getRole() {
  return role;
}public void setRole(be.atc.erpprojetintegration_1.Role role) {
  this.role = role;
}

}