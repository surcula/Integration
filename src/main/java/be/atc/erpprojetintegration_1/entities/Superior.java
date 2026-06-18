package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@NamedQueries({
        @NamedQuery(
                name = "getAllSuperiors",
                query = "SELECT DISTINCT s FROM Superior s " +
                        "JOIN FETCH s.employee e " +
                        "LEFT JOIN FETCH e.employeeDepartments ed " +
                        "LEFT JOIN FETCH ed.department d " +
                        "JOIN FETCH s.superior sup " +
                        "ORDER BY e.lastName, e.firstName"
        ),
        @NamedQuery(
                name = "getActiveSuperiorsBySuperiorId",
                query = "SELECT DISTINCT s FROM Superior s " +
                        "JOIN FETCH s.employee e " +
                        "JOIN FETCH s.superior sup " +
                        "WHERE sup.id = :superiorId " +
                        "AND s.isActive = true " +
                        "AND e.isActive = true " +
                        "ORDER BY e.lastName, e.firstName"
        )
})
@Entity
@Table(name = "employee_superiors")
public class Superior {
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
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "superior_id", nullable = false)
    private Employee superior;

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

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Employee getSuperior() {
        return superior;
    }

    public void setSuperior(Employee superior) {
        this.superior = superior;
    }

}
