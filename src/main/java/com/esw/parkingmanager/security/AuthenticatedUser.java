package com.esw.parkingmanager.security;

import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

// What Spring keeps in the session for a LoggedIn user.
public class AuthenticatedUser implements UserDetails {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final UUID propertyId;
    private final UUID apartmentId;
    private final boolean active;
    private final boolean mustChangePassword;

    public AuthenticatedUser(AppUser user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.role = user.getRole();
        this.propertyId = user.getProperty() == null ? null : user.getProperty().getId();
        this.apartmentId = user.getApartment() == null ? null : user.getApartment().getId();
        this.active = user.isActive();
        this.mustChangePassword = user.isMustChangePassword();
    }

    // hasRole("SUPER_ADMIN") looks for the authority "ROLE_SUPER_ADMIN"
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() { return passwordHash; }

    // Spring calls it "username", here it's the email
    @Override
    public String getUsername() { return email; }

    // A deactivated user can't log in
    @Override
    public boolean isEnabled() { return active; }

    public UUID getId() { return id; }
    public Role getRole() { return role; }
    public UUID getPropertyId() { return propertyId; }
    public UUID getApartmentId() { return apartmentId; }
    public boolean isMustChangePassword() { return mustChangePassword; }
}