package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Property;
import jakarta.validation.constraints.*;

public record PropertyRequest(@NotBlank String name,
                              @NotNull @PositiveOrZero Integer visitorSpotCapacity,
                              @NotNull @PositiveOrZero Integer permitSpotCapacity,
                              @NotNull @Positive Integer visitorPassDurationHours,
                              String mapPdfUrl) {

    /* toEntity() only copies the allowed fields.
    The entity it builds has no property, no apartment and the default status.
    The service then sets those from the URL and the database, as it already does. */
    public Property toEntity() {
        Property property = new Property();
        property.setName(name);
        property.setVisitorSpotCapacity(visitorSpotCapacity);
        property.setPermitSpotCapacity(permitSpotCapacity);
        property.setVisitorPassDurationHours(visitorPassDurationHours);
        property.setMapPdfUrl(mapPdfUrl);
        return property;
    }
}