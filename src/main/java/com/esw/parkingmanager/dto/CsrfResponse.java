package com.esw.parkingmanager.dto;

public record CsrfResponse(String headerName, String token) {
}