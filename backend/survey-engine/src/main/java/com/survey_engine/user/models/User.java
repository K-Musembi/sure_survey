package com.survey_engine.user.models;

import com.survey_engine.common.models.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Model class for User Entity
 * Maps to database table using JPA ORM
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
    name = "User.tenant",
    attributeNodes = @NamedAttributeNode("tenant")
)
public class User extends BaseEntity implements UserDetails {

    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The full name of the user.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * The unique email address of the user, used as the username for authentication.
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * The hashed password of the user.
     */
    @Column(name = "password", nullable = false, length = 100)
    private String password;

    /**
     * The role of the user (e.g., ADMIN, REGULAR).
     */
    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "department", length = 100)
    private String department;

    /**
     * The tenant this user belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable=false, updatable=false)
    private Tenant tenant;

    /**
     * Returns the authorities granted to the user.
     *
     * @return A collection of {@link GrantedAuthority} objects.
     */
    /**
     * Returns the authorities granted to the user. The role is prefixed with "ROLE_"
     * to integrate with Spring Security's role-based authorization.
     *
     * @return A collection of {@link GrantedAuthority} objects.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null || role.isBlank()) {
            return List.of();
        }
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    /**
     * Returns the username used to authenticate the user.
     *
     * @return The email address of the user.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired.
     *
     * @return {@code true} if the user's account is valid (non-expired), {@code false} otherwise.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * @return {@code true} if the user is not locked, {@code false} otherwise.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     *
     * @return {@code true} if the user's credentials are valid (non-expired), {@code false} otherwise.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * @return {@code true} if the user is enabled, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
