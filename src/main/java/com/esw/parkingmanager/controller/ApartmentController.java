package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.MoveOutResponse;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.ApartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import static com.esw.parkingmanager.security.RoleGroups.*;

import java.util.List;
import java.util.UUID;

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
    public Apartment create(@PathVariable UUID propertyId, @Valid @RequestBody Apartment apartment) {
        access.checkProperty(propertyId, ADMINS);
        return apartmentService.create(propertyId, apartment);
    }

    @GetMapping
    public List<Apartment> list(@PathVariable UUID propertyId) {
        access.checkProperty(propertyId, STAFF);
        return apartmentService.list(propertyId);
    }

    // Apartment level here, so a resident can open their own apartment but not a neighbor's
    @GetMapping("/{apartmentId}")
    public Apartment getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return apartmentService.getOne(propertyId, apartmentId);
    }

    @PutMapping("/{apartmentId}")
    public Apartment update(@PathVariable UUID propertyId,
                            @PathVariable UUID apartmentId,
                            @Valid @RequestBody Apartment changes) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return apartmentService.update(propertyId, apartmentId, changes);
    }

    @PostMapping("/{apartmentId}/move-out")
    public MoveOutResponse moveOut(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return apartmentService.moveOut(propertyId, apartmentId);
    }

    @PostMapping("/{apartmentId}/move-in")
    public Apartment moveIn(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, ADMINS);
        return apartmentService.moveIn(propertyId, apartmentId);
    }
}