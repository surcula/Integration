package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "roles_authorization")
public class RolesAuthorization {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 150)
@javax.persistence.Column(name = "role_authorization_name", length = 150)
private java.lang.String roleAuthorizationName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "role_id", nullable = false)
private be.atc.erpprojetintegration_1.Role role;

@javax.validation.constraints.NotNull
@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY, optional = false)
@javax.persistence.JoinColumn(name = "authorization_id", nullable = false)
private be.atc.erpprojetintegration_1.Authorization authorization;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getRoleAuthorizationName() {
  return roleAuthorizationName;
}public void setRoleAuthorizationName(java.lang.String roleAuthorizationName) {
  this.roleAuthorizationName = roleAuthorizationName;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Role getRole() {
  return role;
}public void setRole(be.atc.erpprojetintegration_1.Role role) {
  this.role = role;
}

public be.atc.erpprojetintegration_1.Authorization getAuthorization() {
  return authorization;
}public void setAuthorization(be.atc.erpprojetintegration_1.Authorization authorization) {
  this.authorization = authorization;
}

}