package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "companies")
public class Company {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 200)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "name", nullable = false, length = 200)
private java.lang.String name;

@javax.validation.constraints.Size(max = 20)
@javax.persistence.Column(name = "phone", length = 20)
private java.lang.String phone;

@javax.validation.constraints.Size(max = 100)
@javax.persistence.Column(name = "bank", length = 100)
private java.lang.String bank;

@javax.validation.constraints.Size(max = 34)
@javax.persistence.Column(name = "iban", length = 34)
private java.lang.String iban;

@javax.validation.constraints.Size(max = 500)
@javax.persistence.Column(name = "logo", length = 500)
private java.lang.String logo;

@javax.validation.constraints.Size(max = 150)
@javax.persistence.Column(name = "email", length = 150)
private java.lang.String email;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.Size(max = 255)
@javax.persistence.Column(name = "website")
private java.lang.String website;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "address_id")
private be.atc.erpprojetintegration_1.Address address;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getName() {
  return name;
}public void setName(java.lang.String name) {
  this.name = name;
}

public java.lang.String getPhone() {
  return phone;
}public void setPhone(java.lang.String phone) {
  this.phone = phone;
}

public java.lang.String getBank() {
  return bank;
}public void setBank(java.lang.String bank) {
  this.bank = bank;
}

public java.lang.String getIban() {
  return iban;
}public void setIban(java.lang.String iban) {
  this.iban = iban;
}

public java.lang.String getLogo() {
  return logo;
}public void setLogo(java.lang.String logo) {
  this.logo = logo;
}

public java.lang.String getEmail() {
  return email;
}public void setEmail(java.lang.String email) {
  this.email = email;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public java.lang.String getWebsite() {
  return website;
}public void setWebsite(java.lang.String website) {
  this.website = website;
}

public be.atc.erpprojetintegration_1.Address getAddress() {
  return address;
}public void setAddress(be.atc.erpprojetintegration_1.Address address) {
  this.address = address;
}

}