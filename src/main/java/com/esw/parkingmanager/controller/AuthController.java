package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.ChangePasswordRequest;
import com.esw.parkingmanager.dto.CsrfResponse;
import com.esw.parkingmanager.dto.UserResponse;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AccessGuard access;
    private final UserService userService;

    public AuthController(AccessGuard access, UserService userService) {
        this.access = access;
        this.userService = userService;
    }

    // The frontend calls this before any POST/PUT/DELETE, and again right after login
    @GetMapping("/csrf")
    public CsrfResponse csrf(CsrfToken token) {
        return new CsrfResponse(token.getHeaderName(), token.getToken());
    }

    // Fresh from the database, so the frontend sees mustChangePassword and knows to show the change password screen
    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(access.currentUserIgnoringPasswordChange());
    }

    // Has to work while the password is temporary, that's the whole point
    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(access.currentUserIgnoringPasswordChange(), request);
    }
}