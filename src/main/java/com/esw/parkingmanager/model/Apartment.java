package com.esw.parkingmanager.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
// Unique pair (No duplicate UnitNumbers inside a property)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "unitNumber"}))
public class Apartment {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private  String unitNumber;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false)
    private int maxVehicles;

    @Column(nullable = false)
    private int maxVisitorPasses;

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

    public int getMaxVehicles() {
        return maxVehicles;
    }

    public void setMaxVehicles(int maxVehicles) {
        this.maxVehicles = maxVehicles;
    }

    public int getMaxVisitorPasses() {
        return maxVisitorPasses;
    }

    public void setMaxVisitorPasses(int maxVisitorPasses) {
        this.maxVisitorPasses = maxVisitorPasses;
    }

    public ApartmentStatus getStatus() {
        return status;
    }
}
