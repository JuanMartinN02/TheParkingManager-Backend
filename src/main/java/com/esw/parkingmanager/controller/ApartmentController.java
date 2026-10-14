package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.service.ApartmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/apartments")
public class ApartmentController {

    private final ApartmentService apartmentService;

    public ApartmentController(ApartmentService apartmentService) {
        this.apartmentService = apartmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Apartment create(@PathVariable UUID propertyId, @RequestBody Apartment apartment) {
        return apartmentService.create(propertyId, apartment);
    }

    @GetMapping
    public List<Apartment> list(@PathVariable UUID propertyId) {
        return apartmentService.list(propertyId);
    }

    @GetMapping("/{apartmentId}")
    public Apartment getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        return apartmentService.getOne(propertyId, apartmentId);
    }

    @PutMapping("/{apartmentId}")
    public Apartment update(@PathVariable UUID propertyId,
                            @PathVariable UUID apartmentId,
                            @RequestBody Apartment changes) {
        return apartmentService.update(propertyId, apartmentId, changes);
    }
}