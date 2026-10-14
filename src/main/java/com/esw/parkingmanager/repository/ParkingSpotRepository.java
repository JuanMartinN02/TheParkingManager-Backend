package com.esw.parkingmanager.repository;

import com.esw.parkingmanager.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {
    List<ParkingSpot> findByPropertyId(UUID propertyId);

    Optional<ParkingSpot> findByIdAndPropertyId(UUID id, UUID propertyId);

    boolean existsByPropertyIdAndLabel(UUID propertyId, String label);

    /* the bulk request has 200 labels (example), and we need to know which already exist. Calling existsBy...
    in a loop would mean 200 round-trips to the database. This asks once: “of these 200 labels,
    which are already in this property?” Postgres answers with only the conflicting ones, for example ["A12", "A13"].
    That’s the list the 409 response will show. */
    // This is JPQL, not SQL (SQL but speaks in Java names)
    @Query("SELECT s.label FROM ParkingSpot s WHERE s.property.id = :propertyId AND s.label IN :labels")
    List<String> findExistingLabels(@Param("propertyId") UUID propertyId,
                                    @Param("labels") Collection<String> labels);

}
