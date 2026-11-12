package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.PropertyRequest;
import com.esw.parkingmanager.dto.PropertyResponse;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.PropertyRepository;
import com.esw.parkingmanager.security.AccessGuard;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final AccessGuard access;

    public PropertyController(PropertyRepository propertyRepository, AccessGuard access) {
        this.propertyRepository = propertyRepository;
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PropertyResponse create(@Valid @RequestBody PropertyRequest request) {
        access.checkRole(SUPER_ADMIN_ONLY);
        return PropertyResponse.from(propertyRepository.save(request.toEntity()));
    }

    @GetMapping
    public List<PropertyResponse> list() {
        access.checkRole(SUPER_ADMIN_AND_OPERATOR);
        return propertyRepository.findAll().stream()
                .map(PropertyResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public PropertyResponse getOne(@PathVariable UUID id) {
        access.checkProperty(id, EVERYONE);
        return PropertyResponse.from(findProperty(id));
    }

    @PutMapping("/{id}")
    public PropertyResponse update(@PathVariable UUID id, @Valid @RequestBody PropertyRequest request) {
        access.checkProperty(id, ADMINS);
        Property property = findProperty(id);
        property.setName(request.name());
        property.setVisitorSpotCapacity(request.visitorSpotCapacity());
        property.setPermitSpotCapacity(request.permitSpotCapacity());
        property.setVisitorPassDurationHours(request.visitorPassDurationHours());
        property.setMapPdfUrl(request.mapPdfUrl());
        return PropertyResponse.from(propertyRepository.save(property));
    }

    private Property findProperty(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    }
}