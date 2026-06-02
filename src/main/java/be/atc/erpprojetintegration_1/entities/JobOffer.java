package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_offers")
public class JobOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @NotNull
    @Column(name = "job_offer_name", nullable = false, length = 200)
    private String jobOfferName;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "publish_start_date")
    private LocalDate publishStartDate;

    @Column(name = "publish_end_date")
    private LocalDate publishEndDate;

    @Size(max = 50)
    @Column(name = "status", length = 50)
    private String status;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "function_id", nullable = false)
    private Function function;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJobOfferName() {
        return jobOfferName;
    }

    public void setJobOfferName(String jobOfferName) {
        this.jobOfferName = jobOfferName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public LocalDate getPublishStartDate() {
        return publishStartDate;
    }

    public void setPublishStartDate(LocalDate publishStartDate) {
        this.publishStartDate = publishStartDate;
    }

    public LocalDate getPublishEndDate() {
        return publishEndDate;
    }

    public void setPublishEndDate(LocalDate publishEndDate) {
        this.publishEndDate = publishEndDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

}