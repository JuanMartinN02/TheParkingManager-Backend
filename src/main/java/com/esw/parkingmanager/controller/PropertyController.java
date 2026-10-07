package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.PropertyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyRepository propertyRepository;

    public PropertyController(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    // Create a property
    @PostMapping
    public Property create(@RequestBody Property property){
        return propertyRepository.save(property);
    }

    // Get a List of all properties
    @GetMapping
    public List<Property> list(){
        return propertyRepository.findAll();
    }

    // Get property by ID or throw exception
    @GetMapping("/{id}")
    public Property getOne(@PathVariable UUID id){
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    }

    // Update property
    @PutMapping("/{id}")
    public Property update(@PathVariable UUID id, @RequestBody Property changes){
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
