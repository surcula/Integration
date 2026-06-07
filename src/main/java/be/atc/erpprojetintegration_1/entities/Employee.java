package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.Civilite;
import be.atc.erpprojetintegration_1.enums.Gender;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@NamedQueries({
        @NamedQuery(
                name = "getEmployeeById",
                query = "SELECT e FROM Employee e WHERE e.id = :id"
        ),
        @NamedQuery(
                name = "getEmployeeByEmail",
                query = "SELECT e FROM Employee e LEFT JOIN FETCH e.role WHERE e.email = :email"
        ),
        @NamedQuery(
                name = "getAllActiveEmployees",
                query = "SELECT e FROM Employee e WHERE e.isActive = true ORDER BY e.lastName, e.firstName"
        ),
        @NamedQuery(
                name = "getAllEmployees",
                query = "SELECT e FROM Employee e ORDER BY e.lastName, e.firstName"
        ),
        @NamedQuery(
                name = "getAllActiveEmployeesWithDepartments",
                query = "SELECT DISTINCT e " +
                        "FROM Employee e " +
                        "LEFT JOIN FETCH e.employeeDepartments ed " +
                        "LEFT JOIN FETCH ed.department d " +
                        "WHERE e.isActive = true " +
                        "ORDER BY e.lastName, e.firstName"
        )
})


@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Size(max = 100)
    @NotNull
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Size(max = 150)
    @Column(name = "place_of_birth", length = 150)
    private String placeOfBirth;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 150)
    @NotNull
    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "civilite", length = 20)
    private Civilite civilite;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Size(max = 50)
    @Column(name = "employment_status", length = 50)
    private String employmentStatus;

    @Size(max = 50)
    @NotNull
    @Column(name = "employee_number", nullable = false, length = 50)
    private String employeeNumber;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;


    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY)
    private List<EmployeeDepartment> employeeDepartments = new ArrayList<>();

    public List<EmployeeDepartment> getEmployeeDepartments() {
        return employeeDepartments;
    }

    public void setEmployeeDepartments(List<EmployeeDepartment> employeeDepartments) {
        this.employeeDepartments = employeeDepartments;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Civilite getCivilite() {
        return civilite;
    }

    public void setCivilite(Civilite civilite) {
        this.civilite = civilite;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

}