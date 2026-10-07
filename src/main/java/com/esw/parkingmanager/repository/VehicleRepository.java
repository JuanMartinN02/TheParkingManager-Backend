package com.esw.parkingmanager.repository;

import com.esw.parkingmanager.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    // Number of vehicles in a given apartment
    long countByApartmentId(UUID apartmentId);

    // Does the vehicle exist in this property?
    boolean existsByPropertyIdAndPlate(UUID propertyId, String plate);

    // List of vehicles of an apartment
    List<Vehicle> findByApartmentId(UUID apartmentId);

    // Does this vehicle belong to this apartment?
    Optional<Vehicle> findByIdAndApartmentId(UUID id, UUID apartmentId);
}
