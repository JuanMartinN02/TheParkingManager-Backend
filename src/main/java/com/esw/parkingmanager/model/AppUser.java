package com.esw.parkingmanager.model;

import jakarta.persistence.*;

import java.util.Locale;
import java.util.UUID;

@Entity
// "user" is a reserved word in Postgres, so the table needs another name
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    // Never the real password, only its BCrypt hash
    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Set for PROPERTY_ADMIN and RESIDENT, null for SUPER_ADMIN and TOWING_OPERATOR
    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;

    // Set for RESIDENT only
    @ManyToOne
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    @Column(nullable = false)
    private String preferredLanguage = "en";

    // Users are deactivated, never deleted, so tow records keep their operator
    @Column(nullable = false)
    private boolean active = true;

    // True when someone else set a temporary password for this account
    @Column(nullable = false)
    private boolean mustChangePassword = true;

    public AppUser() {
    }

    // Same rule on save and on login, so Juan@ and juan@ are always the same account
    public static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    public UUID getId() { return id; }

    public String getEmail() { return email; }

    // Emails are stored lowercase so Juan@ and juan@ can't be two different accounts
    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isMustChangePassword() { return mustChangePassword; }
    public void setMustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; }
}