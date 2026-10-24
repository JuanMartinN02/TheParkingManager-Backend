package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.PlateStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record PlateLookupResponse(PlateStatus status,
                                  String plate,
                                  String unitNumber,
                                  VehicleSummary vehicle,
                                  List<AuthorizationSummary> authorizations,
                                  OffsetDateTime checkedAt) {
}