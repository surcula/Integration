package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "categories")
public class Category {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.Size(max = 150)
@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "category_name", nullable = false, length = 150)
private java.lang.String categoryName;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.lang.String getCategoryName() {
  return categoryName;
}public void setCategoryName(java.lang.String categoryName) {
  this.categoryName = categoryName;
}

}