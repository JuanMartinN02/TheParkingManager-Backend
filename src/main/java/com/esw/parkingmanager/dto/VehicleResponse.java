package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Vehicle;

import java.util.UUID;

public record VehicleResponse(UUID id, UUID apartmentId, String plate, String make, String model,
                              Integer year, String color) {

    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(vehicle.getId(), vehicle.getApartment().getId(), vehicle.getPlate(),
                vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getColor());
    }
}