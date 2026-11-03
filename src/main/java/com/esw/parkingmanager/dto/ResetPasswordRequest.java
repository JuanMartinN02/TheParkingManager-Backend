package com.esw.parkingmanager.dto;

import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(@NotBlank String temporaryPassword) {
}