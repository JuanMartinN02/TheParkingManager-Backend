package com.esw.parkingmanager.security;

import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class AccessGuard {

    private final AppUserRepository userRepository;

    public AccessGuard(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void checkProperty(UUID propertyId) {
        if (!canAccessProperty(currentUser(), propertyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this property");
        }
    }

    public void checkApartment(UUID propertyId, UUID apartmentId) {
        AppUser user = currentUser();
        if (!canAccessProperty(user, propertyId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this property");
        }
        // A resident only sees their own apartment and everyone else goes by property
        if (user.getRole() == Role.RESIDENT && !apartmentId.equals(apartmentIdOf(user))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You don't have access to this apartment");
        }
    }

    // Every endpoint goes through this. A user on a temporary password can't do anything until they change it
    public AppUser currentUser() {
        AppUser user = currentUserIgnoringPasswordChange();
        if (user.isMustChangePassword()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Password change required");
        }
        return user;
    }

    // Only for /auth/me and /auth/change-password, the two things a user needs while their password is temporary
    public AppUser currentUserIgnoringPasswordChange() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return userRepository.findById(principal.getId())
                .filter(AppUser::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account is no longer active"));
    }

    private boolean canAccessProperty(AppUser user, UUID propertyId) {
        // No default branch on purpose so a new role won't compile until its access is decided here
        return switch (user.getRole()) {
            case SUPER_ADMIN, TOWING_OPERATOR -> true;
            case PROPERTY_ADMIN, RESIDENT -> propertyId.equals(propertyIdOf(user));
        };
    }

    private UUID propertyIdOf(AppUser user) {
        return user.getProperty() == null ? null : user.getProperty().getId();
    }

    private UUID apartmentIdOf(AppUser user) {
        return user.getApartment() == null ? null : user.getApartment().getId();
    }
}