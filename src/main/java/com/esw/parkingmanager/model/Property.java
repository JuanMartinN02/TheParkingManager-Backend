package com.esw.parkingmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@Entity
public class Property {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    // Integer, not int: a missing value must be rejected, not turned into 0
    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer visitorSpotCapacity;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer permitSpotCapacity;

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer visitorPassDurationHours;

    private String mapPdfUrl;

    public Property() {
    }

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Integer getVisitorSpotCapacity() { return visitorSpotCapacity; }

    public void setVisitorSpotCapacity(Integer visitorSpotCapacity) { this.visitorSpotCapacity = visitorSpotCapacity; }

    public Integer getPermitSpotCapacity() {
        return permitSpotCapacity;
    }

    public void setPermitSpotCapacity(Integer permitSpotCapacity) {
        this.permitSpotCapacity = permitSpotCapacity;
    }

    public Integer getVisitorPassDurationHours() {
        return visitorPassDurationHours;
    }

    public void setVisitorPassDurationHours(Integer visitorPassDurationHours) {
        this.visitorPassDurationHours = visitorPassDurationHours;
    }

    public String getMapPdfUrl() {
        return mapPdfUrl;
    }

    public void setMapPdfUrl(String mapPdfUrl) {
        this.mapPdfUrl = mapPdfUrl;
    }
}

