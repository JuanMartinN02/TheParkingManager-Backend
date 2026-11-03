package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Role;

import java.util.UUID;

public record CreateUserRequest(String email, String temporaryPassword, Role role,
                                UUID propertyId, UUID apartmentId) {
}