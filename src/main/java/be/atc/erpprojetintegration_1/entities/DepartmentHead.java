package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@NamedQueries({
        @NamedQuery(
                name = "getAllActiveDepartmentHeads",
                query = "SELECT dh FROM DepartmentHead dh " +
                        "JOIN FETCH dh.department d " +
                        "JOIN FETCH dh.superior s " +
                        "WHERE dh.isActive = true " +
                        "AND d.isActive = true " +
                        "AND s.isActive = true " +
                        "ORDER BY s.lastName, s.firstName"
        ),
        @NamedQuery(
                name = "getActiveDepartmentHeadBySuperiorAndDepartment",
                query = "SELECT dh FROM DepartmentHead dh " +
                        "JOIN FETCH dh.department d " +
                        "JOIN FETCH dh.superior s " +
                        "WHERE dh.isActive = true " +
                        "AND s.id = :superiorId " +
                        "AND d.id = :departmentId"
        ),
        @NamedQuery(name = "getAllDepartmentHeads",
                query = "SELECT dh FROM DepartmentHead dh JOIN FETCH dh.department JOIN FETCH dh.superior " +
                        "ORDER BY dh.department.departmentName, dh.isActive DESC, dh.startDate DESC"),
        @NamedQuery(name = "getDepartmentHeadById",
                query = "SELECT dh FROM DepartmentHead dh JOIN FETCH dh.department JOIN FETCH dh.superior WHERE dh.id = :id"),
        @NamedQuery(name = "getActiveDepartmentHeadByDepartmentId",
                query = "SELECT dh FROM DepartmentHead dh JOIN FETCH dh.department JOIN FETCH dh.superior " +
                        "WHERE dh.department.id = :departmentId AND dh.isActive = true"),
        @NamedQuery(name = "getActiveDepartmentHeadsByEmployeeId",
                query = "SELECT dh FROM DepartmentHead dh JOIN FETCH dh.department JOIN FETCH dh.superior " +
                        "WHERE dh.superior.id = :employeeId AND dh.isActive = true AND dh.department.isActive = true"),
        @NamedQuery(name = "closeActiveDepartmentHeads",
                query = "UPDATE DepartmentHead dh SET dh.isActive = false, dh.endDate = :endDate " +
                        "WHERE dh.department.id = :departmentId AND dh.isActive = true")
})
@Entity
@Table(name = "department_heads")
public class DepartmentHead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "superior_id", nullable = false)
    private Employee superior;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Employee getSuperior() { return superior; }
    public void setSuperior(Employee superior) { this.superior = superior; }
}
