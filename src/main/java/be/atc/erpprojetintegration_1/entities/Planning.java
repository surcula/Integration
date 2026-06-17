package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import be.atc.erpprojetintegration_1.enums.PlanningStatus;

@NamedQueries({
        @NamedQuery(
                name = "getAllActivePlannings",
                query = "SELECT p FROM Planning p LEFT JOIN FETCH p.department " +
                        "WHERE p.isActive = true AND p.status <> be.atc.erpprojetintegration_1.enums.PlanningStatus.CANCELLED " +
                        "ORDER BY p.date, p.startHour"
        ),
        @NamedQuery(
                name = "getPlanningById",
                query = "SELECT p FROM Planning p LEFT JOIN FETCH p.department WHERE p.id = :planningId"
        ),
        @NamedQuery(
                name = "getActivePlanningsByEmployee",
                query = "SELECT DISTINCT p FROM PlanningsEmployee pe " +
                        "JOIN pe.planning p " +
                        "LEFT JOIN FETCH p.department " +
                        "WHERE pe.employee.id = :employeeId " +
                        "AND pe.isActive = true AND p.isActive = true AND p.status = :published " +
                        "ORDER BY p.date, p.startHour"
        ),
        @NamedQuery(
                name = "getPlanningByIdForEmployee",
                query = "SELECT DISTINCT p FROM PlanningsEmployee pe " +
                        "JOIN pe.planning p " +
                        "LEFT JOIN FETCH p.department " +
                        "WHERE p.id = :planningId AND pe.employee.id = :employeeId " +
                        "AND pe.isActive = true AND p.isActive = true AND p.status = :published"
        ),
        @NamedQuery(
                name = "getActivePlanningsByEmployeeAndDateRange",
                query = "SELECT DISTINCT p FROM PlanningsEmployee pe JOIN pe.planning p " +
                        "WHERE pe.employee.id = :employeeId " +
                        "AND pe.isActive = true AND p.isActive = true AND p.status <> :cancelled " +
                        "AND p.date BETWEEN :startDate AND :endDate"
        ),
        @NamedQuery(
                name = "getPlanningsByMonthAndEmployee",
                query = "SELECT DISTINCT p FROM PlanningsEmployee pe " +
                        "JOIN pe.planning p LEFT JOIN FETCH p.department " +
                        "JOIN FETCH pe.employee e " +
                        "WHERE pe.employee.id = :employeeId " +
                        "AND pe.isActive = true AND p.isActive = true AND p.status <> :cancelled " +
                        "AND FUNCTION('YEAR', p.date) = :year AND FUNCTION('MONTH', p.date) = :month " +
                        "ORDER BY p.date, p.startHour"
        ),
        @NamedQuery(
                name = "getPlanningsByMonthAndDepartment",
                query = "SELECT p FROM Planning p LEFT JOIN FETCH p.department d " +
                        "WHERE p.isActive = true AND p.status <> :cancelled " +
                        "AND d.id = :departmentId " +
                        "AND FUNCTION('YEAR', p.date) = :year AND FUNCTION('MONTH', p.date) = :month " +
                        "ORDER BY p.date, p.startHour"
        )
})
@Entity
@Table(name = "plannings")
public class Planning {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_hour")
    private LocalTime startHour;

    @Column(name = "end_hour")
    private LocalTime endHour;

    @Size(max = 255)
    @Column(name = "note")
    private String note;

    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlanningStatus status;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getStartHour() {
        return startHour;
    }

    public void setStartHour(LocalTime startHour) {
        this.startHour = startHour;
    }

    public LocalTime getEndHour() {
        return endHour;
    }

    public void setEndHour(LocalTime endHour) {
        this.endHour = endHour;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public PlanningStatus getStatus() { return status; }
    public void setStatus(PlanningStatus status) { this.status = status; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

}
