package com.esw.parkingmanager.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"property_id", "label"}))
public class ParkingSpot {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String label;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    public ParkingSpot() {
    }

    public UUID getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Property getProperty() {
        return property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }
}
