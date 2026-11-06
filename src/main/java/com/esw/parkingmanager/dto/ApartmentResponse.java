package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ApartmentStatus;

import java.util.UUID;

public record ApartmentResponse(UUID id, UUID propertyId, String unitNumber, Integer maxVehicles,
                                Integer maxVisitorPasses, ApartmentStatus status) {

    public static ApartmentResponse from(Apartment apartment) {
        return new ApartmentResponse(apartment.getId(), apartment.getProperty().getId(), apartment.getUnitNumber(),
                apartment.getMaxVehicles(), apartment.getMaxVisitorPasses(), apartment.getStatus());
    }
}