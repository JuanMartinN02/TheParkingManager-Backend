package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.repository.ApartmentRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/properties/{propertyId}/apartments")
public class ApartmentController {

    private final PropertyRepository propertyRepository;

    private final ApartmentRepository apartmentRepository;

    public ApartmentController(PropertyRepository propertyRepository, ApartmentRepository apartmentRepository) {
        this.propertyRepository = propertyRepository;
        this.apartmentRepository = apartmentRepository;
    }

}
