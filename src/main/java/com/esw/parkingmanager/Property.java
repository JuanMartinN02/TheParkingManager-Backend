package com.esw.parkingmanager;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Property {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingMode parkingMode;

    @Column(nullable = false)
    private int visitorSpotCapacity;

    public Property() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public ParkingMode getParkingMode() { return parkingMode; }
    public void setParkingMode(ParkingMode parkingMode) { this.parkingMode = parkingMode; }

    public int getVisitorSpotCapacity() { return visitorSpotCapacity; }
    public void setVisitorSpotCapacity(int visitorSpotCapacity) { this.visitorSpotCapacity = visitorSpotCapacity; }
}
