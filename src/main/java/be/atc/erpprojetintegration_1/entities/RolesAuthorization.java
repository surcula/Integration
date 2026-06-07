package be.atc.erpprojetintegration_1.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@NamedQueries({
        @NamedQuery(
                name = "getAuthorizationNamesByRoleId",
                query = "SELECT ra.authorization.authorizationName " +
                        "FROM RolesAuthorization ra " +
                        "WHERE ra.role.id = :roleId " +
                        "AND ra.isActive = true " +
                        "AND ra.authorization.isActive = true " +
                        "AND ra.role.isActive = true"
        )
})

@Entity
@Table(name = "roles_authorization")
public class RolesAuthorization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 150)
    @Column(name = "role_authorization_name", length = 150)
    private String roleAuthorizationName;

    @NotNull
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorization_id", nullable = false)
    private Authorization authorization;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRoleAuthorizationName() {
        return roleAuthorizationName;
    }

    public void setRoleAuthorizationName(String roleAuthorizationName) {
        this.roleAuthorizationName = roleAuthorizationName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

}