package com.esw.parkingmanager.repository;

import com.esw.parkingmanager.model.ParkingAuthorization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingAuthorizationRepository extends JpaRepository<ParkingAuthorization, UUID> {

    // Authorizations by apartment
    List<ParkingAuthorization> findByApartmentId(UUID apartmentId);

    // find specific auth
    Optional<ParkingAuthorization> findByIdAndApartmentId(UUID id, UUID apartmentId);

    // Count of Active authorizations for a given parking spot
    // (spot = X AND validUntil IS NULL)  OR  (validUntil >= today)
    @Query("""
            SELECT COUNT(a) FROM ParkingAuthorization a
            WHERE a.spot.id = :spotId
              AND (a.validUntil IS NULL OR a.validUntil >= :today)
            """)
    long countActiveForSpot(@Param("spotId") UUID spotId,
                            @Param("today") LocalDate today);

    // Count of active authorizations for a given apartment
    @Query("""
            SELECT COUNT(a) FROM ParkingAuthorization a
            WHERE a.apartment.id = :apartmentId
              AND a.spot IS NULL
              AND (a.validUntil IS NULL OR a.validUntil >= :today)
            """)
    long countActivePermitsForApartment(@Param("apartmentId") UUID apartmentId,
                                        @Param("today") LocalDate today);

    // Active parking authorizations by apartment
    @Query("""
        SELECT a FROM ParkingAuthorization a
        WHERE a.apartment.id = :apartmentId
          AND (a.validUntil IS NULL OR a.validUntil >= :today)
        """)
    List<ParkingAuthorization> findActiveForApartment(@Param("apartmentId") UUID apartmentId,
                                                      @Param("today") LocalDate today);

    // Active Authorizations by spot
    // We accepted a tiny race window on double-booking. If two active rows ever existed,
    // an Optional return type would make Spring throw an exception, and the operator
    // would get a 500 in the middle of a lookup. A List never throws. The service will take the first entry.
    @Query("""
        SELECT a FROM ParkingAuthorization a
        WHERE a.spot.id = :spotId
          AND (a.validUntil IS NULL OR a.validUntil >= :today)
        """)
    List<ParkingAuthorization> findActiveForSpot(@Param("spotId") UUID spotId,
                                                 @Param("today") LocalDate today);

    // delete
    void deleteBySpotId(UUID spotId);
}