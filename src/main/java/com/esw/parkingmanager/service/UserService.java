package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.ChangePasswordRequest;
import com.esw.parkingmanager.dto.CreateUserRequest;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ApartmentStatus;
import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.repository.AppUserRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import com.esw.parkingmanager.security.PasswordPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PropertyRepository propertyRepository;
    private final ApartmentService apartmentService;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository userRepository,
                       PropertyRepository propertyRepository,
                       ApartmentService apartmentService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
        this.apartmentService = apartmentService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser create(AppUser creator, CreateUserRequest request) {
        if (request.role() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role is required");
        }
        checkCanCreate(creator, request);

        String email = AppUser.normalizeEmail(request.email());
        if (email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
        }
        PasswordPolicy.check(request.temporaryPassword());

        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.temporaryPassword()));
        user.setRole(request.role());
        // Temporary password, so the user picks their own at first login
        user.setMustChangePassword(true);

        // Which links an account has depends on its role
        switch (request.role()) {
            case SUPER_ADMIN, TOWING_OPERATOR -> {
                if (request.propertyId() != null || request.apartmentId() != null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            request.role() + " works on all properties, so no propertyId or apartmentId");
                }
            }
            case PROPERTY_ADMIN -> {
                if (request.propertyId() == null || request.apartmentId() != null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A property admin needs a propertyId and no apartmentId");
                }
                user.setProperty(propertyRepository.findById(request.propertyId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found")));
            }
            case RESIDENT -> {
                if (request.propertyId() == null || request.apartmentId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "A resident needs a propertyId and an apartmentId");
                }
                // getOne also proves the apartment belongs to that property
                Apartment apartment = apartmentService.getOne(request.propertyId(), request.apartmentId());

                // A moved out apartment gets new residents only after move-in
                if (apartment.getStatus() != ApartmentStatus.ACTIVE) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Apartment is not active");
                }

                user.setApartment(apartment);
                // Copied from the apartment, never from the request, same as Vehicle
                user.setProperty(apartment.getProperty());
            }
        }

        return userRepository.save(user);
    }

    public List<AppUser> list(AppUser viewer) {
        return switch (viewer.getRole()) {
            case SUPER_ADMIN -> userRepository.findAll();
            case PROPERTY_ADMIN -> userRepository.findByPropertyId(viewer.getProperty().getId());
            case RESIDENT, TOWING_OPERATOR ->
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't list users");
        };
    }

    @Transactional
    public AppUser deactivate(AppUser actor, UUID userId) {
        AppUser target = findUser(userId);
        // Blocking this alone means a SUPER_ADMIN always remains: only an active SUPER_ADMIN can deactivate another one
        if (target.getId().equals(actor.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You can't deactivate your own account");
        }
        checkCanManage(actor, target);
        target.setActive(false);
        return userRepository.save(target);
    }

    // No email recovery yet, so a forgotten password means an admin sets a new temporary one
    @Transactional
    public AppUser resetPassword(AppUser actor, UUID userId, String temporaryPassword) {
        AppUser target = findUser(userId);
        checkCanManage(actor, target);
        PasswordPolicy.check(temporaryPassword);
        target.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        target.setMustChangePassword(true);
        return userRepository.save(target);
    }

    @Transactional
    public void changePassword(AppUser user, ChangePasswordRequest request) {
        // Asking for the current password stops someone using a session left open on a shared device
        if (request.currentPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }
        PasswordPolicy.check(request.newPassword());
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    private void checkCanCreate(AppUser creator, CreateUserRequest request) {
        boolean allowed = switch (creator.getRole()) {
            case SUPER_ADMIN -> true;
            // A property admin only adds residents, and only to their own property
            case PROPERTY_ADMIN -> request.role() == Role.RESIDENT
                    && creator.getProperty().getId().equals(request.propertyId());
            case RESIDENT, TOWING_OPERATOR -> false;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't create this kind of account");
        }
    }

    private void checkCanManage(AppUser actor, AppUser target) {
        boolean allowed = switch (actor.getRole()) {
            case SUPER_ADMIN -> true;
            case PROPERTY_ADMIN -> target.getRole() == Role.RESIDENT
                    && target.getProperty() != null
                    && target.getProperty().getId().equals(actor.getProperty().getId());
            case RESIDENT, TOWING_OPERATOR -> false;
        };
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't manage this account");
        }
    }

    private AppUser findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}