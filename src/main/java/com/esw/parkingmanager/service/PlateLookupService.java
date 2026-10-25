package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.AuthorizationSummary;
import com.esw.parkingmanager.dto.PlateLookupResponse;
import com.esw.parkingmanager.dto.SpotLookupResponse;
import com.esw.parkingmanager.dto.VehicleSummary;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ParkingAuthorization;
import com.esw.parkingmanager.model.ParkingSpot;
import com.esw.parkingmanager.model.PlateStatus;
import com.esw.parkingmanager.model.Vehicle;
import com.esw.parkingmanager.repository.ParkingAuthorizationRepository;
import com.esw.parkingmanager.repository.ParkingSpotRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import com.esw.parkingmanager.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PlateLookupService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSpotRepository parkingSpotRepository;
    private final ParkingAuthorizationRepository authorizationRepository;
    private final PropertyRepository propertyRepository;
    private final Clock clock;

    public PlateLookupService(VehicleRepository vehicleRepository,
                              ParkingSpotRepository parkingSpotRepository,
                              ParkingAuthorizationRepository authorizationRepository,
                              PropertyRepository propertyRepository,
                              Clock clock) {
        this.vehicleRepository = vehicleRepository;
        this.parkingSpotRepository = parkingSpotRepository;
        this.authorizationRepository = authorizationRepository;
        this.propertyRepository = propertyRepository;
        this.clock = clock;
    }

    // Read only. a lookup never writes anything
    @Transactional(readOnly = true)
    public PlateLookupResponse lookupPlate(UUID propertyId, String rawPlate) {
        checkPropertyExists(propertyId);

        // One "now" for the whole check, in Houston time. The date comes from the same moment
        OffsetDateTime checkedAt = OffsetDateTime.now(clock);
        LocalDate today = checkedAt.toLocalDate();

        // Same normalizer as on save, otherwise a registered car could come back UNKNOWN
        String plate = PlateNormalizer.normalize(rawPlate);
        if (plate.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plate is required");
        }

        // Not registered in this property is a valid answer, not an error
        Optional<Vehicle> found = vehicleRepository.findByPropertyIdAndPlate(propertyId, plate);
        if (found.isEmpty()) {
            return new PlateLookupResponse(PlateStatus.UNKNOWN, plate, null, null, List.of(), checkedAt);
        }

        Vehicle vehicle = found.get();
        Apartment apartment = vehicle.getApartment();

        // Permits AND reserved spots both count here
        List<AuthorizationSummary> authorizations = authorizationRepository
                .findActiveForApartment(apartment.getId(), today)
                .stream()
                .map(AuthorizationSummary::from)
                .toList();

        // A known car with no active authorization is a dangerous case
        PlateStatus status = authorizations.isEmpty()
                ? PlateStatus.RESIDENT_REGISTERED
                : PlateStatus.RESIDENT_AUTHORIZED;

        return new PlateLookupResponse(status, plate, apartment.getUnitNumber(),
                VehicleSummary.from(vehicle), authorizations, checkedAt);
    }

    @Transactional(readOnly = true)
    public SpotLookupResponse lookupSpot(UUID propertyId, String rawLabel) {
        checkPropertyExists(propertyId);

        OffsetDateTime checkedAt = OffsetDateTime.now(clock);
        LocalDate today = checkedAt.toLocalDate();

        String label = SpotLabelNormalizer.normalize(rawLabel);
        if (label.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Label is required");
        }

        // An unknown spot could be a typo in the search
        ParkingSpot spot = parkingSpotRepository.findByPropertyIdAndLabel(propertyId, label)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));

        List<ParkingAuthorization> active = authorizationRepository.findActiveForSpot(spot.getId(), today);
        if (active.isEmpty()) {
            return new SpotLookupResponse(spot.getLabel(), false, null, null, List.of(), checkedAt);
        }

        // Rule 1 allows only one active, take the first in case a race ever created two
        ParkingAuthorization authorization = active.get(0);
        Apartment apartment = authorization.getApartment();

        // Authorization belongs to the apartment, so any of its cars can use the spot
        List<VehicleSummary> vehicles = vehicleRepository.findByApartmentId(apartment.getId())
                .stream()
                .map(VehicleSummary::from)
                .toList();

        return new SpotLookupResponse(spot.getLabel(), true, apartment.getUnitNumber(),
                authorization.getValidUntil(), vehicles, checkedAt);
    }

    // A wrong property id would return UNKNOWN for every plate, so fail loudly instead
    private void checkPropertyExists(UUID propertyId) {
        if (!propertyRepository.existsById(propertyId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
    }
}