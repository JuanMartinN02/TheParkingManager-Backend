package com.esw.parkingmanager.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Property {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int visitorSpotCapacity;

    @Column(nullable = false)
    private int permitSpotCapacity;

    @Column(nullable = false)
    private int visitorPassDurationHours;

    private String mapPdfUrl;

    public Property() {
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getVisitorSpotCapacity() { return visitorSpotCapacity; }

    public void setVisitorSpotCapacity(int visitorSpotCapacity) { this.visitorSpotCapacity = visitorSpotCapacity; }

    public int getPermitSpotCapacity() {
        return permitSpotCapacity;
    }

    public void setPermitSpotCapacity(int permitSpotCapacity) {
        this.permitSpotCapacity = permitSpotCapacity;
    }

    public int getVisitorPassDurationHours() {
        return visitorPassDurationHours;
    }

    public void setVisitorPassDurationHours(int visitorPassDurationHours) {
        this.visitorPassDurationHours = visitorPassDurationHours;
    }

    public String getMapPdfUrl() {
        return mapPdfUrl;
    }

    public void setMapPdfUrl(String mapPdfUrl) {
        this.mapPdfUrl = mapPdfUrl;
    }
}

