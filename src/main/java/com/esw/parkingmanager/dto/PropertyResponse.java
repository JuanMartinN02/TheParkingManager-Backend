package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Property;

import java.util.UUID;

public record PropertyResponse(UUID id, String name, Integer visitorSpotCapacity, Integer permitSpotCapacity,
                               Integer visitorPassDurationHours, String mapPdfUrl) {

    public static PropertyResponse from(Property property) {
        return new PropertyResponse(property.getId(), property.getName(), property.getVisitorSpotCapacity(),
                property.getPermitSpotCapacity(), property.getVisitorPassDurationHours(), property.getMapPdfUrl());
    }
}