package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.ApartmentRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/apartments")
public class ApartmentController {

    private final PropertyRepository propertyRepository;

    private final ApartmentRepository apartmentRepository;

    public ApartmentController(PropertyRepository propertyRepository, ApartmentRepository apartmentRepository) {
        this.propertyRepository = propertyRepository;
        this.apartmentRepository = apartmentRepository;
    }

    // Create apartment
    @PostMapping
    public Apartment create(@PathVariable UUID propertyId, @RequestBody Apartment apartment){
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        apartment.setProperty(property);

        return  apartmentRepository.save(apartment);
    }

    // Get a List of all apartments
    @GetMapping
    public List<Apartment> list(@PathVariable UUID propertyId) {
        return apartmentRepository.findByPropertyId(propertyId);
    }

    // Get an apartment
    @GetMapping("/{apartmentId}")
    public Apartment getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        return apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found"));
    }

    // Update apartment
    @PutMapping("/{apartmentId}")
    public Apartment update(@PathVariable UUID propertyId,
                            @PathVariable UUID apartmentId,
                            @RequestBody Apartment changes) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new RuntimeException("Apartment not found"));
        apartment.setUnitNumber(changes.getUnitNumber());
        apartment.setMaxVehicles(changes.getMaxVehicles());
        apartment.setMaxVisitorPasses(changes.getMaxVisitorPasses());
        apartment.setStatus(changes.getStatus());
        return apartmentRepository.save(apartment);
    }
}
