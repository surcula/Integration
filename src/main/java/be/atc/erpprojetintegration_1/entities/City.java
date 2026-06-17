package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@NamedQueries({
        @NamedQuery(
                name = "getAllActiveCities",
                query = "SELECT c FROM City c WHERE c.active = true ORDER BY c.cityName"
        ),
        @NamedQuery(
                name = "getActiveCitiesByZip",
                query = "SELECT c FROM City c WHERE c.active = true AND c.zipCode = :zip ORDER BY c.cityName"
        ),
        @NamedQuery(
                name = "getAllCities",
                query = "SELECT c FROM City c ORDER BY c.cityName, c.zipCode"
        )
})
@Entity
@Table(name = "cities")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 100)
    @NotNull
    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    @NotNull
    @Column(name = "zip_code", nullable = false)
    private Integer zipCode;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public Integer getZipCode() {
        return zipCode;
    }

    public void setZipCode(Integer zipCode) {
        this.zipCode = zipCode;
    }

    public Boolean getIsActive() {
        return active;
    }

    public void setIsActive(Boolean active) {
        this.active = active;
    }

}
