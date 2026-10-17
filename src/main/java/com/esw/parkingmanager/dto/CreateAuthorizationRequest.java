package com.esw.parkingmanager.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAuthorizationRequest(UUID spotId, LocalDate validUntil) {
}