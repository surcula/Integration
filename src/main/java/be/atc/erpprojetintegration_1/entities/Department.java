package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "departments")
public class Department {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 150)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "department_name", nullable = false, length = 150)
private java.lang.String departmentName;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

@javax.validation.constraints.Size(max = 20)
@javax.persistence.Column(name = "phone", length = 20)
private java.lang.String phone;

@javax.validation.constraints.Size(max = 150)
@javax.persistence.Column(name = "email", length = 150)
private java.lang.String email;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "department_head_id")
private be.atc.erpprojetintegration_1.DepartmentHead departmentHead;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getDepartmentName() {
  return departmentName;
}public void setDepartmentName(java.lang.String departmentName) {
  this.departmentName = departmentName;
}

public java.lang.String getDescription() {
  return description;
}public void setDescription(java.lang.String description) {
  this.description = description;
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

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.DepartmentHead getDepartmentHead() {
  return departmentHead;
}public void setDepartmentHead(be.atc.erpprojetintegration_1.DepartmentHead departmentHead) {
  this.departmentHead = departmentHead;
}

}