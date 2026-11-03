package com.esw.parkingmanager.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

// One password rule for every place a password is set
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 12;

    private PasswordPolicy() {
    }

    public static void check(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must be at least " + MIN_LENGTH + " characters");
        }
    }
}