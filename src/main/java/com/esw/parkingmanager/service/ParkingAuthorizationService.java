package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.CreateAuthorizationRequest;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ApartmentStatus;
import com.esw.parkingmanager.model.ParkingAuthorization;
import com.esw.parkingmanager.model.ParkingSpot;
import com.esw.parkingmanager.repository.ParkingAuthorizationRepository;
import com.esw.parkingmanager.repository.ParkingSpotRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ParkingAuthorizationService {

    private final ParkingAuthorizationRepository authorizationRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ApartmentService apartmentService;
    private final Clock clock;

    public ParkingAuthorizationService(ParkingAuthorizationRepository authorizationRepository,
                                       ParkingSpotRepository parkingSpotRepository,
                                       ApartmentService apartmentService,
                                       Clock clock) {
        this.authorizationRepository = authorizationRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.apartmentService = apartmentService;
        this.clock = clock;
    }

    @Transactional
    public ParkingAuthorization create(UUID propertyId, UUID apartmentId, CreateAuthorizationRequest request) {
        LocalDate today = LocalDate.now(clock);

        // Rule 4: apartment in this property and active
        Apartment apartment = apartmentService.getOne(propertyId, apartmentId);
        if (apartment.getStatus() != ApartmentStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Apartment is not active");
        }

        // Rule 3: no expiry in the past
        validateNotInPast(request.validUntil(), today);

        ParkingAuthorization authorization = new ParkingAuthorization();
        authorization.setApartment(apartment);
        authorization.setProperty(apartment.getProperty());
        authorization.setValidUntil(request.validUntil());

        if (request.spotId() == null) { // For permit parking (request.spotId() == null)
            // Rule 2: one active permit per apartment
            if (authorizationRepository.countActivePermitsForApartment(apartmentId, today) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Apartment already has an active permit. Renew it instead");
            }
        } else { // For reserved parking
            // Rule 4: spot must be in this property
            ParkingSpot spot = parkingSpotRepository.findByIdAndPropertyId(request.spotId(), propertyId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
            // Rule 1: no double-booking
            if (authorizationRepository.countActiveForSpot(spot.getId(), today) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Spot " + spot.getLabel() + " is already assigned");
            }
            authorization.setSpot(spot);
        }

        return authorizationRepository.save(authorization);
    }

    public List<ParkingAuthorization> list(UUID propertyId, UUID apartmentId) {
        apartmentService.getOne(propertyId, apartmentId);
        return authorizationRepository.findByApartmentId(apartmentId);
    }

    public ParkingAuthorization getOne(UUID propertyId, UUID apartmentId, UUID authorizationId) {
        apartmentService.getOne(propertyId, apartmentId);
        return authorizationRepository.findByIdAndApartmentId(authorizationId, apartmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Authorization not found"));
    }

    // Rule 5: renew changes only validUntil
    @Transactional
    public ParkingAuthorization renew(UUID propertyId, UUID apartmentId, UUID authorizationId, LocalDate newValidUntil) {
        LocalDate today = LocalDate.now(clock);
        ParkingAuthorization authorization = getOne(propertyId, apartmentId, authorizationId);

        validateNotInPast(newValidUntil, today);

        // Reviving an EXPIRED authorization must not create a second active one
        if (!authorization.isActiveOn(today)) {
            if (authorization.getSpot() != null
                    && authorizationRepository.countActiveForSpot(authorization.getSpot().getId(), today) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Spot " + authorization.getSpot().getLabel() + " has been assigned to someone else since this expired");
            }
            if (authorization.getSpot() == null
                    && authorizationRepository.countActivePermitsForApartment(apartmentId, today) > 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Apartment already has another active permit");
            }
        }

        authorization.setValidUntil(newValidUntil);
        return authorizationRepository.save(authorization);
    }

    @Transactional
    public void delete(UUID propertyId, UUID apartmentId, UUID authorizationId) {
        ParkingAuthorization authorization = getOne(propertyId, apartmentId, authorizationId);
        authorizationRepository.delete(authorization);
    }

    private void validateNotInPast(LocalDate validUntil, LocalDate today) {
        if (validUntil != null && validUntil.isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validUntil cannot be in the past");
        }
    }
}