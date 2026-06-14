package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.PlanningSwapStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@NamedQueries({
        @NamedQuery(
                name = "cancelPendingSwapRequestsByPlanning",
                query = "UPDATE PlanningEmployeeSwapRequest sr " +
                        "SET sr.status = :cancelled, sr.isActive = false, sr.reviewedAt = :reviewedAt " +
                        "WHERE sr.planningEmployee.planning.id = :planningId AND sr.status = :pending"
        ),
        @NamedQuery(
                name = "cancelPendingSwapRequestsByAssignment",
                query = "UPDATE PlanningEmployeeSwapRequest sr SET sr.status = :cancelled, " +
                        "sr.reviewedAt = :reviewedAt WHERE sr.planningEmployee.id = :assignmentId " +
                        "AND sr.status = :pending AND sr.isActive = true"
        ),
        @NamedQuery(
                name = "countPendingSwapRequestsByAssignment",
                query = "SELECT COUNT(sr) FROM PlanningEmployeeSwapRequest sr " +
                        "WHERE sr.planningEmployee.id = :assignmentId " +
                        "AND sr.status = :status AND sr.isActive = true"
        ),
        @NamedQuery(
                name = "getAllSwapRequests",
                query = "SELECT DISTINCT sr FROM PlanningEmployeeSwapRequest sr " +
                        "JOIN FETCH sr.planningEmployee pe JOIN FETCH pe.planning p " +
                        "JOIN FETCH pe.employee assigned JOIN FETCH sr.requestedBy requester " +
                        "LEFT JOIN FETCH p.department " +
                        "LEFT JOIN FETCH sr.replacementEmployee replacement " +
                        "LEFT JOIN FETCH sr.reviewedBy reviewer " +
                        "WHERE sr.isActive = true " +
                        "ORDER BY CASE WHEN sr.status = :pending THEN 0 ELSE 1 END, sr.requestedAt DESC"
        ),
        @NamedQuery(
                name = "getSwapRequestsByEmployee",
                query = "SELECT DISTINCT sr FROM PlanningEmployeeSwapRequest sr " +
                        "JOIN FETCH sr.planningEmployee pe JOIN FETCH pe.planning p " +
                        "JOIN FETCH pe.employee assigned JOIN FETCH sr.requestedBy requester " +
                        "LEFT JOIN FETCH p.department " +
                        "LEFT JOIN FETCH sr.replacementEmployee replacement " +
                        "LEFT JOIN FETCH sr.reviewedBy reviewer " +
                        "WHERE sr.isActive = true AND requester.id = :employeeId " +
                        "ORDER BY CASE WHEN sr.status = :pending THEN 0 ELSE 1 END, sr.requestedAt DESC"
        ),
        @NamedQuery(
                name = "getSwapRequestById",
                query = "SELECT sr FROM PlanningEmployeeSwapRequest sr " +
                        "JOIN FETCH sr.planningEmployee pe JOIN FETCH pe.planning p " +
                        "JOIN FETCH pe.employee JOIN FETCH sr.requestedBy " +
                        "LEFT JOIN FETCH p.department LEFT JOIN FETCH sr.replacementEmployee " +
                        "LEFT JOIN FETCH sr.reviewedBy WHERE sr.id = :requestId AND sr.isActive = true"
        )
})
@Entity
@Table(name = "planning_employee_swap_requests")
public class PlanningEmployeeSwapRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "planning_employee_id", nullable = false)
    private PlanningsEmployee planningEmployee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_employee_id", nullable = false)
    private Employee requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replacement_employee_id")
    private Employee replacementEmployee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_employee_id")
    private Employee reviewedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PlanningSwapStatus status;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    @Size(max = 500)
    @Column(name = "review_comment", length = 500)
    private String reviewComment;

    @NotNull
    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @Column(name = "emergency_mode", nullable = false)
    private Boolean emergencyMode = false;

    @NotNull
    @Column(name = "relaunch_required", nullable = false)
    private Boolean relaunchRequired = false;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public PlanningsEmployee getPlanningEmployee() { return planningEmployee; }
    public void setPlanningEmployee(PlanningsEmployee planningEmployee) { this.planningEmployee = planningEmployee; }
    public Employee getRequestedBy() { return requestedBy; }
    public void setRequestedBy(Employee requestedBy) { this.requestedBy = requestedBy; }
    public Employee getReplacementEmployee() { return replacementEmployee; }
    public void setReplacementEmployee(Employee replacementEmployee) { this.replacementEmployee = replacementEmployee; }
    public Employee getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(Employee reviewedBy) { this.reviewedBy = reviewedBy; }
    public PlanningSwapStatus getStatus() { return status; }
    public void setStatus(PlanningSwapStatus status) { this.status = status; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }
    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
    public Boolean getEmergencyMode() { return emergencyMode; }
    public void setEmergencyMode(Boolean emergencyMode) { this.emergencyMode = emergencyMode; }
    public Boolean getRelaunchRequired() { return relaunchRequired; }
    public void setRelaunchRequired(Boolean relaunchRequired) { this.relaunchRequired = relaunchRequired; }
}
