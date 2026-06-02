package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "training_centers")
public class TrainingCenter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 200)
    @Column(name = "training_career_name", length = 200)
    private String trainingCareerName;

    @Size(max = 20)
    @Column(name = "phone", length = 20)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTrainingCareerName() {
        return trainingCareerName;
    }

    public void setTrainingCareerName(String trainingCareerName) {
        this.trainingCareerName = trainingCareerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

}