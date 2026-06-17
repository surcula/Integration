package be.atc.erpprojetintegration_1.entities;

import be.atc.erpprojetintegration_1.enums.CandidateApplicationStatus;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Entité JPA liée à la table job_offers_candidates.
 * Elle représente la candidature d'un candidat à une offre précise.
 */
@NamedQueries({
        @NamedQuery(
                name = "getAllJobOfferCandidates",
                query = "SELECT joc FROM JobOffersCandidate joc " +
                        "LEFT JOIN FETCH joc.candidate " +
                        "LEFT JOIN FETCH joc.jobOffers jo " +
                        "LEFT JOIN FETCH jo.function " +
                        "ORDER BY joc.id DESC"
        ),
        @NamedQuery(
                name = "getJobOfferCandidateById",
                query = "SELECT joc FROM JobOffersCandidate joc " +
                        "LEFT JOIN FETCH joc.candidate " +
                        "LEFT JOIN FETCH joc.jobOffers jo " +
                        "LEFT JOIN FETCH jo.function " +
                        "WHERE joc.id = :id"
        ),
        @NamedQuery(
                name = "getActiveApplicationByCandidateEmailAndJobOffer",
                query = "SELECT joc FROM JobOffersCandidate joc " +
                        "JOIN joc.candidate c " +
                        "JOIN joc.jobOffers jo " +
                        "WHERE joc.isActive = true " +
                        "AND LOWER(c.email) = LOWER(:email) " +
                        "AND jo.id = :jobOfferId"
        )
})
@Entity
@Table(name = "job_offers_candidates")
public class JobOffersCandidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @Column(name = "job_offer_name", length = 200)
    private String jobOfferName;

    @NotNull
    @Column(name = "application_date", nullable = false)
    private LocalDate applicationDate;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "application_status", length = 50)
    private CandidateApplicationStatus applicationStatus;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_offers_id", nullable = false)
    private JobOffer jobOffers;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

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

    public LocalDate getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDate applicationDate) {
        this.applicationDate = applicationDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public CandidateApplicationStatus getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(CandidateApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public JobOffer getJobOffers() {
        return jobOffers;
    }

    public void setJobOffers(JobOffer jobOffers) {
        this.jobOffers = jobOffers;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

}
