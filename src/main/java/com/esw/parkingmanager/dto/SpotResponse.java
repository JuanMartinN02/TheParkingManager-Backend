package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.ParkingSpot;

import java.util.UUID;

public record SpotResponse(UUID id, String label) {

    public static SpotResponse from(ParkingSpot spot) {
        return new SpotResponse(spot.getId(), spot.getLabel());
    }
}