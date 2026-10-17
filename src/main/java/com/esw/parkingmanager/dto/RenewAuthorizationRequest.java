package com.esw.parkingmanager.dto;

import java.time.LocalDate;

public record RenewAuthorizationRequest(LocalDate validUntil) {
}