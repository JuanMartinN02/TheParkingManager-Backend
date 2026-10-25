package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Vehicle;

public record VehicleSummary(String plate, String make, String model, Integer year, String color) {

    public static VehicleSummary from(Vehicle vehicle) {
        return new VehicleSummary(vehicle.getPlate(), vehicle.getMake(), vehicle.getModel(),
                vehicle.getYear(), vehicle.getColor());
    }
}