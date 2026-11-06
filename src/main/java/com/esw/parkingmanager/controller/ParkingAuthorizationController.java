package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.AuthorizationResponse;
import com.esw.parkingmanager.dto.CreateAuthorizationRequest;
import com.esw.parkingmanager.dto.RenewAuthorizationRequest;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.ParkingAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;

@RestController
@RequestMapping("/properties/{propertyId}/apartments/{apartmentId}/authorizations")
public class ParkingAuthorizationController {

    private final ParkingAuthorizationService authorizationService;
    private final AccessGuard access;

    public ParkingAuthorizationController(ParkingAuthorizationService authorizationService, AccessGuard access) {
        this.authorizationService = authorizationService;
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorizationResponse create(@PathVariable UUID propertyId, @PathVariable UUID apartmentId,
                                        @RequestBody CreateAuthorizationRequest request) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return AuthorizationResponse.from(authorizationService.create(propertyId, apartmentId, request));
    }

    @GetMapping
    public List<AuthorizationResponse> list(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return authorizationService.list(propertyId, apartmentId).stream()
                .map(AuthorizationResponse::from)
                .toList();
    }

    @GetMapping("/{authorizationId}")
    public AuthorizationResponse getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId,
                                        @PathVariable UUID authorizationId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return AuthorizationResponse.from(authorizationService.getOne(propertyId, apartmentId, authorizationId));
    }

    @PutMapping("/{authorizationId}")
    public AuthorizationResponse renew(@PathVariable UUID propertyId, @PathVariable UUID apartmentId,
                                       @PathVariable UUID authorizationId,
                                       @RequestBody RenewAuthorizationRequest request) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return AuthorizationResponse.from(
                authorizationService.renew(propertyId, apartmentId, authorizationId, request.validUntil()));
    }

    @DeleteMapping("/{authorizationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId, @PathVariable UUID apartmentId,
                       @PathVariable UUID authorizationId) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        authorizationService.delete(propertyId, apartmentId, authorizationId);
    }
}