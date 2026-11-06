package com.esw.parkingmanager.dto;

// What the admin sees after a move-out, so nothing disappears without them knowing
public record MoveOutResponse(String unitNumber,
                              long authorizationsRemoved,
                              long vehiclesRemoved,
                              int accountsDeactivated) {
}