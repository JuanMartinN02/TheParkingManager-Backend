package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.MoveOutResponse;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ApartmentStatus;
import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.*;
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
    private final ParkingAuthorizationRepository authorizationRepository;
    private final AppUserRepository userRepository;

    public ApartmentService(ApartmentRepository apartmentRepository,
                            PropertyRepository propertyRepository,
                            VehicleRepository vehicleRepository,
                            ParkingAuthorizationRepository authorizationRepository,
                            AppUserRepository userRepository) {
        this.apartmentRepository = apartmentRepository;
        this.propertyRepository = propertyRepository;
        this.vehicleRepository = vehicleRepository;
        this.authorizationRepository = authorizationRepository;
        this.userRepository = userRepository;
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

    @Transactional // All or nothing, a half done move-out could leave a former resident's car authorized
    public MoveOutResponse moveOut(UUID propertyId, UUID apartmentId) {
        Apartment apartment = getOne(propertyId, apartmentId);
        if (apartment.getStatus() != ApartmentStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apartment is already moved out");
        }

        // Frees the apartment's reserved spots for the next tenant
        long authorizationsRemoved = authorizationRepository.deleteByApartmentId(apartmentId);
        long vehiclesRemoved = vehicleRepository.deleteByApartmentId(apartmentId);

        // Deactivated, not deleted, so their account still exists for history
        List<AppUser> residents = userRepository.findByApartmentIdAndActiveTrue(apartmentId);
        residents.forEach(resident -> resident.setActive(false));
        userRepository.saveAll(residents);

        apartment.setStatus(ApartmentStatus.MOVED_OUT);
        apartmentRepository.save(apartment);

        return new MoveOutResponse(apartment.getUnitNumber(), authorizationsRemoved, vehiclesRemoved, residents.size());
    }

    @Transactional
    public Apartment moveIn(UUID propertyId, UUID apartmentId) {
        Apartment apartment = getOne(propertyId, apartmentId);
        if (apartment.getStatus() != ApartmentStatus.MOVED_OUT) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apartment is already active");
        }
        apartment.setStatus(ApartmentStatus.ACTIVE);
        return apartmentRepository.save(apartment);
    }
}