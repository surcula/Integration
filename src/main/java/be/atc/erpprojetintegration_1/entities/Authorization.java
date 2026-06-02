package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "authorizations")
public class Authorization {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 150)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "authorization_name", nullable = false, length = 150)
private java.lang.String authorizationName;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getAuthorizationName() {
  return authorizationName;
}public void setAuthorizationName(java.lang.String authorizationName) {
  this.authorizationName = authorizationName;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

}