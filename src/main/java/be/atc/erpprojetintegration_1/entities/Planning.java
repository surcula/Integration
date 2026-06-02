package be.atc.erpprojetintegration_1;
@javax.persistence.Entity
@javax.persistence.Table(name = "plannings")
public class Planning {
@javax.persistence.Id
@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
@javax.persistence.Column(name = "id", nullable = false)
private java.lang.Integer id;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "date", nullable = false)
private java.time.LocalDate date;

@javax.persistence.Column(name = "start_hour")
private java.time.LocalTime startHour;

@javax.persistence.Column(name = "end_hour")
private java.time.LocalTime endHour;

@javax.validation.constraints.Size(max = 255)
@javax.persistence.Column(name = "note")
private java.lang.String note;

@javax.validation.constraints.Size(max = 50)
@javax.persistence.Column(name = "type", length = 50)
private java.lang.String type;

@javax.persistence.Lob
@javax.persistence.Column(name = "description")
private java.lang.String description;

@javax.validation.constraints.NotNull
@javax.persistence.Column(name = "is_active", nullable = false)
private java.lang.Boolean isActive;

@javax.persistence.ManyToOne(fetch = javax.persistence.FetchType.LAZY)
@javax.persistence.JoinColumn(name = "department_id")
private be.atc.erpprojetintegration_1.Department department;

public java.lang.Integer getId() {
  return id;
}public void setId(java.lang.Integer id) {
  this.id = id;
}

public java.time.LocalDate getDate() {
  return date;
}public void setDate(java.time.LocalDate date) {
  this.date = date;
}

public java.time.LocalTime getStartHour() {
  return startHour;
}public void setStartHour(java.time.LocalTime startHour) {
  this.startHour = startHour;
}

public java.time.LocalTime getEndHour() {
  return endHour;
}public void setEndHour(java.time.LocalTime endHour) {
  this.endHour = endHour;
}

public java.lang.String getNote() {
  return note;
}public void setNote(java.lang.String note) {
  this.note = note;
}

public java.lang.String getType() {
  return type;
}public void setType(java.lang.String type) {
  this.type = type;
}

public java.lang.String getDescription() {
  return description;
}public void setDescription(java.lang.String description) {
  this.description = description;
}

public java.lang.Boolean getIsActive() {
  return isActive;
}public void setIsActive(java.lang.Boolean isActive) {
  this.isActive = isActive;
}

public be.atc.erpprojetintegration_1.Department getDepartment() {
  return department;
}public void setDepartment(be.atc.erpprojetintegration_1.Department department) {
  this.department = department;
}

}