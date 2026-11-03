package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;

import java.util.UUID;

// Never return AppUser itself: it would send the password hash in the JSON
public record UserResponse(UUID id, String email, Role role, UUID propertyId,
                           UUID apartmentId, boolean active, boolean mustChangePassword) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(),
                user.getProperty() == null ? null : user.getProperty().getId(),
                user.getApartment() == null ? null : user.getApartment().getId(),
                user.isActive(), user.isMustChangePassword());
    }
}