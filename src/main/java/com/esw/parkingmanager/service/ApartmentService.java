package com.esw.parkingmanager.service;

import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.ApartmentRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import com.esw.parkingmanager.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final PropertyRepository propertyRepository;
    private final VehicleRepository vehicleRepository;

    public ApartmentService(ApartmentRepository apartmentRepository,
                            PropertyRepository propertyRepository,
                            VehicleRepository vehicleRepository) {
        this.apartmentRepository = apartmentRepository;
        this.propertyRepository = propertyRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Transactional
    public Apartment create(UUID propertyId, Apartment apartment) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        apartment.setProperty(property);
        return apartmentRepository.save(apartment);
    }

    public List<Apartment> list(UUID propertyId) {
        return apartmentRepository.findByPropertyId(propertyId);
    }

    public Apartment getOne(UUID propertyId, UUID apartmentId) {
        return apartmentRepository.findByIdAndPropertyId(apartmentId, propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apartment not found"));
    }

    @Transactional
    public Apartment update(UUID propertyId, UUID apartmentId, Apartment changes) {
        Apartment apartment = getOne(propertyId, apartmentId);

        // Rule: the limit can't drop below the cars already registered
        long currentVehicles = vehicleRepository.countByApartmentId(apartmentId);
        if (changes.getMaxVehicles() < currentVehicles) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Apartment has " + currentVehicles + " vehicles. Remove some before lowering the limit to "
                            + changes.getMaxVehicles());
        }

        apartment.setUnitNumber(changes.getUnitNumber());
        apartment.setMaxVehicles(changes.getMaxVehicles());
        apartment.setMaxVisitorPasses(changes.getMaxVisitorPasses());
        return apartmentRepository.save(apartment);
    }
}