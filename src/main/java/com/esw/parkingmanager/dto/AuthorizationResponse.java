package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.ParkingAuthorization;
import com.esw.parkingmanager.model.ParkingSpot;

import java.time.LocalDate;
import java.util.UUID;

// spotId and spotLabel are null for a permit
public record AuthorizationResponse(UUID id, UUID apartmentId, UUID spotId, String spotLabel, LocalDate validUntil) {

    public static AuthorizationResponse from(ParkingAuthorization authorization) {
        ParkingSpot spot = authorization.getSpot();
        return new AuthorizationResponse(authorization.getId(), authorization.getApartment().getId(),
                spot == null ? null : spot.getId(),
                spot == null ? null : spot.getLabel(),
                authorization.getValidUntil());
    }
}