package com.esw.parkingmanager.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
public class ParkingAuthorization {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    @ManyToOne
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    // null = permit (open area); set = reserved spot
    @ManyToOne
    @JoinColumn(name = "spot_id")
    private ParkingSpot spot;

    // null = never expires; inclusive (valid through the whole day)
    private LocalDate validUntil;

    public ParkingAuthorization() {
    }

    public boolean isActiveOn(LocalDate date) {
        return validUntil == null || !validUntil.isBefore(date);
    }

    public UUID getId() { return id; }

    public Apartment getApartment() { return apartment; }
    public void setApartment(Apartment apartment) { this.apartment = apartment; }

    public Property getProperty() { return property; }
    public void setProperty(Property property) { this.property = property; }

    public ParkingSpot getSpot() { return spot; }
    public void setSpot(ParkingSpot spot) { this.spot = spot; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }
}