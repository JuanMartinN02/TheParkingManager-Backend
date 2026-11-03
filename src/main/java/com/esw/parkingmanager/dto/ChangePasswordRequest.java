package com.esw.parkingmanager.dto;

public record ChangePasswordRequest(String currentPassword, String newPassword) {
}