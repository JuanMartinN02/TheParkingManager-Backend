package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.ApartmentRequest;
import com.esw.parkingmanager.dto.ApartmentResponse;
import com.esw.parkingmanager.dto.MoveOutResponse;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.ApartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;

@RestController
@RequestMapping("/properties/{propertyId}/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;
    private final AccessGuard access;

    public ApartmentController(ApartmentService apartmentService, AccessGuard access) {
        this.apartmentService = apartmentService;
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApartmentResponse create(@PathVariable UUID propertyId, @Valid @RequestBody ApartmentRequest request) {
        access.checkProperty(propertyId, ADMINS);
        return ApartmentResponse.from(apartmentService.create(propertyId, request.toEntity()));
    }

    @GetMapping
    public List<ApartmentResponse> list(@PathVariable UUID propertyId) {
        access.checkProperty(propertyId, STAFF);
        return apartmentService.list(propertyId).stream()
                .map(ApartmentResponse::from)
                .toList();
    }

    // Apartment level here, so a resident can open their own apartment but not a neighbor's
    @GetMapping("/{apartmentId}")
    public ApartmentResponse getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return ApartmentResponse.from(apartmentService.getOne(propertyId, apartmentId));
    }

    @PutMapping("/{apartmentId}")
    public ApartmentResponse update(@PathVariable UUID propertyId,
                                    @PathVariable UUID apartmentId,
                                    @Valid @RequestBody ApartmentRequest request) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return ApartmentResponse.from(apartmentService.update(propertyId, apartmentId, request.toEntity()));
    }

    @PostMapping("/{apartmentId}/move-out")
    public MoveOutResponse moveOut(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return apartmentService.moveOut(propertyId, apartmentId);
    }

    @PostMapping("/{apartmentId}/move-in")
    public ApartmentResponse moveIn(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return ApartmentResponse.from(apartmentService.moveIn(propertyId, apartmentId));
    }
}