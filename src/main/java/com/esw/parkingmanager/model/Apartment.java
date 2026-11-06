package com.esw.parkingmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@Entity
// Unique pair (No duplicate UnitNumbers inside a property)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "unitNumber"}))
public class Apartment {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String unitNumber;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    // Integer, not int: a missing limit must be rejected, not turned into 0
    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer maxVehicles;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer maxVisitorPasses;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApartmentStatus status = ApartmentStatus.ACTIVE;

    public Apartment() {
    }

    public UUID getId() {
        return id;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public Integer getMaxVehicles() {
        return maxVehicles;
    }

    public void setMaxVehicles(Integer maxVehicles) {
        this.maxVehicles = maxVehicles;
    }

    public Integer getMaxVisitorPasses() {
        return maxVisitorPasses;
    }

    public void setMaxVisitorPasses(Integer maxVisitorPasses) {
        this.maxVisitorPasses = maxVisitorPasses;
    }

    public ApartmentStatus getStatus() {
        return status;
    }

    public void setStatus(ApartmentStatus status) {
        this.status = status;
    }
}
