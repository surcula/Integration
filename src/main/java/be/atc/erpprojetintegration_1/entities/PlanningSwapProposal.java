package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.PlanningSwapProposalStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@NamedQueries({
        @NamedQuery(name = "getSwapProposalsByEmployee",
                query = "SELECT sp FROM PlanningSwapProposal sp " +
                        "JOIN FETCH sp.swapRequest sr JOIN FETCH sr.planningEmployee pe " +
                        "JOIN FETCH pe.planning p JOIN FETCH pe.employee " +
                        "JOIN FETCH sr.requestedBy JOIN FETCH sp.proposedEmployee " +
                        "LEFT JOIN FETCH p.department LEFT JOIN FETCH sp.proposedBy " +
                        "WHERE sp.proposedEmployee.id = :employeeId AND sp.isActive = true " +
                        "ORDER BY CASE WHEN sp.status = :pending THEN 0 ELSE 1 END, sp.proposedAt DESC"),
        @NamedQuery(name = "getSwapProposalsByRequest",
                query = "SELECT sp FROM PlanningSwapProposal sp " +
                        "JOIN FETCH sp.proposedEmployee LEFT JOIN FETCH sp.proposedBy " +
                        "WHERE sp.swapRequest.id = :requestId AND sp.isActive = true ORDER BY sp.proposedAt DESC"),
        @NamedQuery(name = "getPendingSwapProposals",
                query = "SELECT sp FROM PlanningSwapProposal sp JOIN FETCH sp.swapRequest sr " +
                        "JOIN FETCH sr.planningEmployee pe JOIN FETCH pe.planning " +
                        "WHERE sp.status = :pending AND sp.isActive = true"),
        @NamedQuery(name = "countPendingSwapProposalsByRequest",
                query = "SELECT COUNT(sp) FROM PlanningSwapProposal sp WHERE sp.swapRequest.id = :requestId " +
                        "AND sp.status = :pending AND sp.isActive = true"),
        @NamedQuery(name = "cancelOtherSwapProposals",
                query = "UPDATE PlanningSwapProposal sp SET sp.status = :cancelled, sp.respondedAt = :respondedAt " +
                        "WHERE sp.swapRequest.id = :requestId AND sp.id <> :proposalId AND sp.status = :pending"),
        @NamedQuery(name = "cancelPendingSwapProposalsByRequest",
                query = "UPDATE PlanningSwapProposal sp SET sp.status = :cancelled, sp.respondedAt = :respondedAt " +
                        "WHERE sp.swapRequest.id = :requestId AND sp.status = :pending")
})
@Entity
@Table(name = "planning_swap_proposals",
        uniqueConstraints = @UniqueConstraint(name = "uk_swap_proposal_employee", columnNames = {"swap_request_id", "proposed_employee_id"}))
public class PlanningSwapProposal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "swap_request_id", nullable = false)
    private PlanningEmployeeSwapRequest swapRequest;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposed_employee_id", nullable = false)
    private Employee proposedEmployee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "proposed_by_employee_id", nullable = false)
    private Employee proposedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanningSwapProposalStatus status;

    @NotNull
    @Column(name = "proposed_at", nullable = false)
    private LocalDateTime proposedAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Integer getId() { return id; }
    public PlanningEmployeeSwapRequest getSwapRequest() { return swapRequest; }
    public void setSwapRequest(PlanningEmployeeSwapRequest swapRequest) { this.swapRequest = swapRequest; }
    public Employee getProposedEmployee() { return proposedEmployee; }
    public void setProposedEmployee(Employee proposedEmployee) { this.proposedEmployee = proposedEmployee; }
    public Employee getProposedBy() { return proposedBy; }
    public void setProposedBy(Employee proposedBy) { this.proposedBy = proposedBy; }
    public PlanningSwapProposalStatus getStatus() { return status; }
    public void setStatus(PlanningSwapProposalStatus status) { this.status = status; }
    public LocalDateTime getProposedAt() { return proposedAt; }
    public void setProposedAt(LocalDateTime proposedAt) { this.proposedAt = proposedAt; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }
}
