package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.JobOfferStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité JPA liée à la table job_offers.
 * Elle contient les informations de l'offre, son statut de publication et la fonction associée.
 */
@NamedQueries({
        @NamedQuery(
                name = "getAllJobOffers",
                query = "SELECT jo FROM JobOffer jo LEFT JOIN FETCH jo.function ORDER BY jo.createAt DESC"
        ),
        @NamedQuery(
                name = "getAllActiveJobOffers",
                query = "SELECT jo FROM JobOffer jo LEFT JOIN FETCH jo.function WHERE jo.isActive = true ORDER BY jo.createAt DESC"
        ),
        @NamedQuery(
                name = "getActiveJobOffersByFunctionId",
                query = "SELECT jo FROM JobOffer jo LEFT JOIN FETCH jo.function " +
                        "WHERE jo.isActive = true AND jo.status = be.atc.erpprojetintegration_1.enums.JobOfferStatus.PUBLISHED " +
                        "AND jo.function.id = :functionId ORDER BY jo.createAt DESC"
        ),
        @NamedQuery(
                name = "getPublishedActiveJobOffers",
                query = "SELECT jo FROM JobOffer jo LEFT JOIN FETCH jo.function f LEFT JOIN FETCH f.city " +
                        "WHERE jo.isActive = true AND jo.status = :status ORDER BY jo.publishStartDate DESC, jo.createAt DESC"
        ),
        @NamedQuery(
                name = "getPublishedActiveJobOfferById",
                query = "SELECT jo FROM JobOffer jo LEFT JOIN FETCH jo.function f LEFT JOIN FETCH f.city " +
                        "WHERE jo.id = :id AND jo.isActive = true AND jo.status = :status"
        ),
        @NamedQuery(
                name = "getJobOfferById",
                query = "SELECT jo FROM JobOffer jo LEFT JOIN FETCH jo.function WHERE jo.id = :id"
        )
})
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

    @Size(max = 150)
    @Column(name = "email", length = 150)
    private String email;

    @Size(max = 150)
    @Column(name = "contact", length = 150)
    private String contact;

    @Size(max = 100)
    @Column(name = "duration", length = 100)
    private String duration;

    @Column(name = "number_of_open_positions")
    private Integer numberOfOpenPositions;

    @Lob
    @Column(name = "profil")
    private String profil;

    @Lob
    @Column(name = "job_description")
    private String jobDescription;

    @Lob
    @Column(name = "requirements")
    private String requirements;

    @Lob
    @Column(name = "comments")
    private String comments;

    @NotNull
    @Column(name = "create_at", nullable = false)
    private LocalDateTime createAt;

    @Column(name = "publish_start_date")
    private LocalDate publishStartDate;

    @Column(name = "publish_end_date")
    private LocalDate publishEndDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private JobOfferStatus status;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Integer getNumberOfOpenPositions() {
        return numberOfOpenPositions;
    }

    public void setNumberOfOpenPositions(Integer numberOfOpenPositions) {
        this.numberOfOpenPositions = numberOfOpenPositions;
    }

    public String getProfil() {
        return profil;
    }

    public void setProfil(String profil) {
        this.profil = profil;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
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

    public JobOfferStatus getStatus() {
        return status;
    }

    public void setStatus(JobOfferStatus status) {
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
