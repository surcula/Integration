package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.PlanningStatus;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@NamedQueries({
        @NamedQuery(
                name = "getActiveEmployeesByPlanning",
                query = "SELECT pe.employee FROM PlanningsEmployee pe " +
                        "WHERE pe.planning.id = :planningId AND pe.isActive = true " +
                        "ORDER BY pe.employee.lastName, pe.employee.firstName"
        ),
        @NamedQuery(
                name = "getActiveAssignmentByPlanningAndEmployee",
                query = "SELECT pe FROM PlanningsEmployee pe " +
                        "JOIN FETCH pe.planning p JOIN FETCH pe.employee e " +
                        "LEFT JOIN FETCH p.department " +
                        "WHERE p.id = :planningId AND e.id = :employeeId " +
                        "AND pe.isActive = true AND p.isActive = true AND p.status <> :cancelled"
        ),
        @NamedQuery(
                name = "getActiveAssignmentsByPlanning",
                query = "SELECT pe FROM PlanningsEmployee pe JOIN FETCH pe.employee " +
                        "WHERE pe.planning.id = :planningId AND pe.isActive = true"
        ),
        @NamedQuery(
                name = "getCandidateAssignmentsForConflict",
                query = "SELECT pe FROM PlanningsEmployee pe JOIN FETCH pe.employee e " +
                        "JOIN FETCH pe.planning p WHERE pe.isActive = true AND p.isActive = true " +
                        "AND p.status <> :cancelled " +
                        "AND e.id IN :employeeIds AND p.date BETWEEN :candidateStart AND :candidateEnd " +
                        "ORDER BY e.lastName, e.firstName"
        )
})
@Entity
@Table(name = "plannings_employees")
public class PlanningsEmployee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Size(max = 500)
    @Column(name = "note", length = 500)
    private String note;

    @NotNull
    @Column(name = "performed", nullable = false)
    private Boolean performed = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_id", nullable = false)
    private Planning planning;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Planning getPlanning() {
        return planning;
    }

    public void setPlanning(Planning planning) {
        this.planning = planning;
    }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Boolean getPerformed() { return performed; }
    public void setPerformed(Boolean performed) { this.performed = performed; }

    public BigDecimal calculateHours() {
        if (planning == null || planning.getStartHour() == null || planning.getEndHour() == null) {
            return BigDecimal.ZERO;
        }
        long minutes = Duration.between(planning.getStartHour(), planning.getEndHour()).toMinutes();
        if (minutes < 0) {
            minutes += 24 * 60;
        }
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros();
    }

}
