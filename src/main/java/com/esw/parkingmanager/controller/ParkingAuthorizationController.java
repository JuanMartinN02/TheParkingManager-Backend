package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.CreateAuthorizationRequest;
import com.esw.parkingmanager.dto.RenewAuthorizationRequest;
import com.esw.parkingmanager.model.ParkingAuthorization;
import com.esw.parkingmanager.service.ParkingAuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/apartments/{apartmentId}/authorizations")
public class ParkingAuthorizationController {

    private final ParkingAuthorizationService authorizationService;

    public ParkingAuthorizationController(ParkingAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingAuthorization create(@PathVariable UUID propertyId,
                                       @PathVariable UUID apartmentId,
                                       @RequestBody CreateAuthorizationRequest request) {
        return authorizationService.create(propertyId, apartmentId, request);
    }

    @GetMapping
    public List<ParkingAuthorization> list(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        return authorizationService.list(propertyId, apartmentId);
    }

    @GetMapping("/{authorizationId}")
    public ParkingAuthorization getOne(@PathVariable UUID propertyId,
                                       @PathVariable UUID apartmentId,
                                       @PathVariable UUID authorizationId) {
        return authorizationService.getOne(propertyId, apartmentId, authorizationId);
    }

    @PutMapping("/{authorizationId}")
    public ParkingAuthorization renew(@PathVariable UUID propertyId,
                                      @PathVariable UUID apartmentId,
                                      @PathVariable UUID authorizationId,
                                      @RequestBody RenewAuthorizationRequest request) {
        return authorizationService.renew(propertyId, apartmentId, authorizationId, request.validUntil());
    }

    @DeleteMapping("/{authorizationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId,
                       @PathVariable UUID apartmentId,
                       @PathVariable UUID authorizationId) {
        authorizationService.delete(propertyId, apartmentId, authorizationId);
    }
}