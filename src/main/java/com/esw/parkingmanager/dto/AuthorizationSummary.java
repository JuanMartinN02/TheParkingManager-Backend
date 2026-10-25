package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.ParkingAuthorization;

import java.time.LocalDate;

public record AuthorizationSummary(String spotLabel, LocalDate validUntil) {

    // static ... from(...) is a factory method that converts an entity into a DTO.
    public static AuthorizationSummary from(ParkingAuthorization authorization) {
        // A permit has no spot, so calling getSpot().getLabel() directly would throw a NullPointerException.
        String label = authorization.getSpot() == null ? null : authorization.getSpot().getLabel();
        return new AuthorizationSummary(label, authorization.getValidUntil());
    }
}