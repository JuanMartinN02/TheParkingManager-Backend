package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Apartment;
import jakarta.validation.constraints.*;

// No status here: it only changes through move-out / move-in
public record ApartmentRequest(@NotBlank String unitNumber,
                               @NotNull @PositiveOrZero Integer maxVehicles,
                               @NotNull @PositiveOrZero Integer maxVisitorPasses) {

    /* toEntity() only copies the allowed fields.
    The entity it builds has no property, no apartment and the default status.
    The service then sets those from the URL and the database, as it already does. */
    public Apartment toEntity() {
        Apartment apartment = new Apartment();
        apartment.setUnitNumber(unitNumber);
        apartment.setMaxVehicles(maxVehicles);
        apartment.setMaxVisitorPasses(maxVisitorPasses);
        return apartment;
    }
}