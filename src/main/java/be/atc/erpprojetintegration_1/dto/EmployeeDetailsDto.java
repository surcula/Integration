package be.atc.erpprojetintegration_1.dto;

import java.io.Serializable;

/**
 * Read-only employee information displayed in detail dialogs.
 */
public class EmployeeDetailsDto implements Serializable {

    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String birthDate;
    private String placeOfBirth;
    private String civilite;
    private String gender;
    private String employmentStatus;
    private String employeeNumber;
    private String departmentName;
    private String streetName;
    private String streetNumber;
    private String boxNumber;
    private String zipCode;
    private String cityName;
    private Boolean active;

    public String getFullName() {
        return ((firstName != null ? firstName : "") + " "
                + (lastName != null ? lastName : "")).trim();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public String getPlaceOfBirth() { return placeOfBirth; }
    public void setPlaceOfBirth(String placeOfBirth) { this.placeOfBirth = placeOfBirth; }
    public String getCivilite() { return civilite; }
    public void setCivilite(String civilite) { this.civilite = civilite; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getBoxNumber() { return boxNumber; }
    public void setBoxNumber(String boxNumber) { this.boxNumber = boxNumber; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
