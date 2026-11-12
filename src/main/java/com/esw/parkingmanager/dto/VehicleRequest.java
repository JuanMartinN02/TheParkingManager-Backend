package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Vehicle;
import jakarta.validation.constraints.*;

public record VehicleRequest(@NotBlank String plate,
                             @NotBlank String make,
                             @NotBlank String model,
                             // Catches typos like 20219 or 219
                             @NotNull @Min(1900) @Max(2300) Integer year,
                             @NotBlank String color) {

    /* toEntity() only copies the allowed fields.
    The entity it builds has no property, no apartment and the default status.
    The service then sets those from the URL and the database, as it already does. */
    public Vehicle toEntity() {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(plate);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setColor(color);
        return vehicle;
    }
}