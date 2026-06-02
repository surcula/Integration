package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "trainings")
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @Column(name = "training_certificate_name", length = 200)
    private String trainingCertificateName;

    @Size(max = 50)
    @Column(name = "code", length = 50)
    private String code;

    @Column(name = "validity_months")
    private Integer validityMonths;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTrainingCertificateName() {
        return trainingCertificateName;
    }

    public void setTrainingCertificateName(String trainingCertificateName) {
        this.trainingCertificateName = trainingCertificateName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getValidityMonths() {
        return validityMonths;
    }

    public void setValidityMonths(Integer validityMonths) {
        this.validityMonths = validityMonths;
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

}