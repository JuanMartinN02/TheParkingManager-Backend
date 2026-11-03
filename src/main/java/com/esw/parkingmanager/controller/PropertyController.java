package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.PropertyRepository;
import com.esw.parkingmanager.security.AccessGuard;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyRepository propertyRepository;
    private final AccessGuard access;

    public PropertyController(PropertyRepository propertyRepository, AccessGuard access) {
        this.propertyRepository = propertyRepository;
        this.access = access;
    }

    // SUPER_ADMIN only, the role rule comes in 6d
    @PostMapping
    public Property create(@RequestBody Property property) {
        return propertyRepository.save(property);
    }

    // For now any logged in user sees every property. 6d limits this by role
    @GetMapping
    public List<Property> list() {
        return propertyRepository.findAll();
    }

    @GetMapping("/{id}")
    public Property getOne(@PathVariable UUID id) {
        access.checkProperty(id);
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    }

    @PutMapping("/{id}")
    public Property update(@PathVariable UUID id, @RequestBody Property changes) {
        access.checkProperty(id);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        property.setName(changes.getName());
        property.setVisitorSpotCapacity(changes.getVisitorSpotCapacity());
        property.setPermitSpotCapacity(changes.getPermitSpotCapacity());
        property.setVisitorPassDurationHours(changes.getVisitorPassDurationHours());
        property.setMapPdfUrl(changes.getMapPdfUrl());
        return propertyRepository.save(property);
    }
}