package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.ParkingSpot;
import jakarta.validation.constraints.NotBlank;

public record SpotRequest(@NotBlank String label) {

    /* toEntity() only copies the allowed fields.
    The entity it builds has no property, no apartment and the default status.
    The service then sets those from the URL and the database, as it already does. */
    public ParkingSpot toEntity() {
        ParkingSpot spot = new ParkingSpot();
        spot.setLabel(label);
        return spot;
    }
}