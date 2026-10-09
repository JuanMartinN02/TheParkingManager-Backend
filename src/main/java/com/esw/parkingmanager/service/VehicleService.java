package com.esw.parkingmanager.service;

import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ApartmentStatus;
import com.esw.parkingmanager.model.Vehicle;
import com.esw.parkingmanager.repository.ApartmentRepository;
import com.esw.parkingmanager.repository.VehicleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ApartmentRepository apartmentRepository;

    public VehicleService(ApartmentRepository apartmentRepository, VehicleRepository vehicleRepository) {
        this.apartmentRepository = apartmentRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // @Transactional wraps the whole method in one database transaction. Either everything in it commits, or nothing does.
    @Transactional
    public Vehicle create(UUID propertyId, UUID apartmentId, Vehicle vehicle){
        // Rule 1: apartment must belong to this property and be active
        Apartment apartment = findApartment(propertyId, apartmentId);
        if (apartment.getStatus() != ApartmentStatus.ACTIVE){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apartment is not active");
        }

        // Rule 2: respect the apartment's editable vehicle limit
        long currentVehicles = vehicleRepository.countByApartmentId(apartmentId);
        if (currentVehicles >= apartment.getMaxVehicles()){
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Apartment already has its maximum of " + apartment.getMaxVehicles() + " vehicles. Has " + currentVehicles);
        }

        // Rule 3: normalized plate, unique within the property
        String plate = PlateNormalizer.normalize(vehicle.getPlate());
        if (plate.isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plate is required");
        }
        if (vehicleRepository.existsByPropertyIdAndPlate(propertyId, plate)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plate is already registered in this property");
        }


        // Server-controlled fields: never trust the client for these
        vehicle.setPlate(plate);
        vehicle.setApartment(apartment);
        vehicle.setProperty(apartment.getProperty());

        return vehicleRepository.save(vehicle);
    }

    // List of all vehicles of an apartment
    public List<Vehicle> list(UUID propertyId, UUID apartmentId){
        findApartment(propertyId, apartmentId);
        return vehicleRepository.findByApartmentId(apartmentId);
    }

    // Get a vehicle
    public Vehicle getOne(UUID propertyId, UUID apartmentId, UUID vehicleId){
        findApartment(propertyId, apartmentId);
        return findVehicle(apartmentId, vehicleId);
    }

    // Rule 4: deleting frees a slot; maxVehicles is NOT touched
    @Transactional
    public void delete(UUID propertyId, UUID apartmentId, UUID vehicleId) {
        findApartment(propertyId, apartmentId);
        Vehicle vehicle = findVehicle(apartmentId, vehicleId);
        vehicleRepository.delete(vehicle);
    }

    // Find an apartment and make sure its in a specific property
    private Apartment findApartment(UUID propertyId, UUID apartmentId){
        return apartmentRepository.findByIdAndPropertyId(apartmentId, propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Apartment not found"));
    }

    // Find a vehicle and make sure is in this apartment
    private Vehicle findVehicle(UUID apartmentId, UUID vehicleId){
        return vehicleRepository.findByIdAndApartmentId(vehicleId, apartmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }
}
