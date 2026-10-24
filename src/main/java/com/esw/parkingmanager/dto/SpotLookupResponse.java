package com.esw.parkingmanager.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record SpotLookupResponse(String spotLabel,
                                 boolean assigned,
                                 String unitNumber,
                                 LocalDate validUntil,
                                 List<VehicleSummary> vehicles,
                                 OffsetDateTime checkedAt) {
}