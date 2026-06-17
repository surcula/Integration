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
        )
})
@Entity
@Table(name = "department_heads")
public class DepartmentHead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "superior_id")
    private Employee superior;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
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

    public Employee getSuperior() {
        return superior;
    }

    public void setSuperior(Employee superior) {
        this.superior = superior;
    }
}
