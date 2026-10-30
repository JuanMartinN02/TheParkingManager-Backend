package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.security.AuthenticatedUser;

import java.util.UUID;

public record CurrentUserResponse(UUID id, String email, Role role, UUID propertyId,
                                  UUID apartmentId, boolean mustChangePassword) {

    public static CurrentUserResponse from(AuthenticatedUser user) {
        return new CurrentUserResponse(user.getId(), user.getUsername(), user.getRole(),
                user.getPropertyId(), user.getApartmentId(), user.isMustChangePassword());
    }
}