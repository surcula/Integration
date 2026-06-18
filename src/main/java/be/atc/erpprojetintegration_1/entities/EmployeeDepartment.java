package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;



@NamedQueries({
        @NamedQuery(
                name = "getActiveEmployeeDepartmentByEmployeeId",
                query = "SELECT ed " +
                        "FROM EmployeeDepartment ed " +
                        "JOIN FETCH ed.employee e " +
                        "LEFT JOIN FETCH e.address a " +
                        "LEFT JOIN FETCH a.city c " +
                        "JOIN FETCH ed.department d " +
                        "WHERE e.id = :employeeId " +
                        "AND ed.isActive = true " +
                        "AND d.isActive = true"
        ),
        @NamedQuery(
                name = "getAllActiveEmployeeDepartments",
                query = "SELECT ed " +
                        "FROM EmployeeDepartment ed " +
                        "JOIN FETCH ed.employee e " +
                        "JOIN FETCH ed.department d " +
                        "WHERE ed.isActive = true " +
                        "AND e.isActive = true " +
                        "AND d.isActive = true " +
                        "ORDER BY e.lastName, e.firstName"
        ),
        @NamedQuery(
                name = "getActiveEmployeeDepartmentsByDepartmentId",
                query = "SELECT ed " +
                        "FROM EmployeeDepartment ed " +
                        "JOIN FETCH ed.employee e " +
                        "JOIN FETCH ed.department d " +
                        "WHERE d.id = :departmentId " +
                        "AND ed.isActive = true " +
                        "AND e.isActive = true " +
                        "AND d.isActive = true " +
                        "ORDER BY e.lastName, e.firstName"
        ),
        @NamedQuery(
                name = "getAllEmployeeDepartments",
                query = "SELECT ed FROM EmployeeDepartment ed " +
                        "JOIN FETCH ed.employee e JOIN FETCH ed.department d " +
                        "ORDER BY ed.isActive DESC, e.lastName, e.firstName, ed.startDate DESC"
        ),
        @NamedQuery(
                name = "getEmployeeDepartmentById",
                query = "SELECT ed FROM EmployeeDepartment ed " +
                        "JOIN FETCH ed.employee JOIN FETCH ed.department WHERE ed.id = :id"
        ),
        @NamedQuery(
                name = "closeActiveEmployeeDepartments",
                query = "UPDATE EmployeeDepartment ed SET ed.isActive = false, ed.endDate = :endDate " +
                        "WHERE ed.employee.id = :employeeId AND ed.isActive = true"
        )
})
@Entity
@Table(name = "employees_departments")
public class EmployeeDepartment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

}
