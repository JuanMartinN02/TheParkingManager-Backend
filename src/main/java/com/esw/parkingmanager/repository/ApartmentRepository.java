package com.esw.parkingmanager.repository;

import com.esw.parkingmanager.model.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {
    // Basically: SELECT a FROM Apartment a WHERE a.propertyId = :propertyId
    // Only bring the Properties apartment (Multitenancy) property boundary filter
    // to make sure a request for one property only loads apartments belonging to that specific property.
    List<Apartment> findByPropertyId(UUID propertyId);
}