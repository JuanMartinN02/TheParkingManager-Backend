package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.CreateUserRequest;
import com.esw.parkingmanager.dto.ResetPasswordRequest;
import com.esw.parkingmanager.dto.UserResponse;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final AccessGuard access;

    public UserController(UserService userService, AccessGuard access) {
        this.userService = userService;
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody CreateUserRequest request) {
        return UserResponse.from(userService.create(access.currentUser(), request));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.list(access.currentUser()).stream()
                .map(UserResponse::from)
                .toList();
    }

    @PostMapping("/{userId}/deactivate")
    public UserResponse deactivate(@PathVariable UUID userId) {
        return UserResponse.from(userService.deactivate(access.currentUser(), userId));
    }

    @PostMapping("/{userId}/reset-password")
    public UserResponse resetPassword(@PathVariable UUID userId, @RequestBody ResetPasswordRequest request) {
        return UserResponse.from(userService.resetPassword(access.currentUser(), userId, request.temporaryPassword()));
    }
}