package com.esw.parkingmanager.dto;

import com.esw.parkingmanager.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateUserRequest(@NotBlank @Email String email,
                                @NotBlank String temporaryPassword,
                                @NotNull Role role,
                                UUID propertyId,
                                UUID apartmentId) {
}