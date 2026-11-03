package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.CsrfResponse;
import com.esw.parkingmanager.dto.UserResponse;
import com.esw.parkingmanager.security.AccessGuard;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public AuthController(AccessGuard access, UserService userService) {
        this.access = access;
        this.userService = userService;
    }

    // The frontend calls this before any POST/PUT/DELETE and again right after login
    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    // Fresh from the database, so the frontend sees mustChangePassword and knows to show the change password screen
    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(access.currentUserIgnoringPasswordChange());
    }
}