package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.PlateLookupResponse;
import com.esw.parkingmanager.dto.SpotLookupResponse;
import com.esw.parkingmanager.model.*;
import com.esw.parkingmanager.repository.ParkingAuthorizationRepository;
import com.esw.parkingmanager.repository.ParkingSpotRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import com.esw.parkingmanager.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlateLookupServiceTest {

    // 03:00 UTC on Jan 1 is still 21:00 on Dec 31 in Houston, so every test also proves the Houston date is used
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2027-01-01T03:00:00Z"), ZoneId.of("America/Chicago"));
    private static final LocalDate TODAY = LocalDate.of(2026, 12, 31);
    private static final UUID PROPERTY_ID = UUID.randomUUID();

    @Mock private VehicleRepository vehicleRepository;
    @Mock private ParkingSpotRepository parkingSpotRepository;
    @Mock private ParkingAuthorizationRepository authorizationRepository;
    @Mock private PropertyRepository propertyRepository;

    private PlateLookupService service;

    @BeforeEach
    void setUp() {
        service = new PlateLookupService(vehicleRepository, parkingSpotRepository,
                authorizationRepository, propertyRepository, CLOCK);
    }

    // ---------- Plate lookup ----------

    @Test
    void unknownPlateIsAValidAnswerNotAnError() {
        propertyExists();
        when(vehicleRepository.findByPropertyIdAndPlate(PROPERTY_ID, "ZZZ999")).thenReturn(Optional.empty());

        PlateLookupResponse response = service.lookupPlate(PROPERTY_ID, "ZZZ999");

        assertThat(response.status()).isEqualTo(PlateStatus.UNKNOWN);
        assertThat(response.vehicle()).isNull();
        assertThat(response.authorizations()).isEmpty();
        // No car found, so there is nothing to check authorizations for
        verifyNoInteractions(authorizationRepository);
    }

    @Test
    void carWithReservedSpotIsAuthorized() {
        propertyExists();
        Apartment apartment = apartment("301");
        carIsRegistered(apartment, "AUTH001");
        activeAuthorizationsAre(reserved(apartment, "R1", TODAY));

        PlateLookupResponse response = service.lookupPlate(PROPERTY_ID, "AUTH001");

        assertThat(response.status()).isEqualTo(PlateStatus.RESIDENT_AUTHORIZED);
        assertThat(response.unitNumber()).isEqualTo("301");
        assertThat(response.authorizations()).hasSize(1);
        assertThat(response.authorizations().get(0).spotLabel()).isEqualTo("R1");
    }

    @Test
    void carWithOnlyAPermitIsAuthorized() {
        // A permit has no spot. If it gets lost on the way, a valid resident reads as REGISTERED
        propertyExists();
        Apartment apartment = apartment("302");
        carIsRegistered(apartment, "PERM002");
        activeAuthorizationsAre(permit(apartment, null));

        PlateLookupResponse response = service.lookupPlate(PROPERTY_ID, "PERM002");

        assertThat(response.status()).isEqualTo(PlateStatus.RESIDENT_AUTHORIZED);
        assertThat(response.authorizations().get(0).spotLabel()).isNull();
    }

    @Test
    void carWithNoActiveAuthorizationIsRegistered() {
        propertyExists();
        Apartment apartment = apartment("303");
        carIsRegistered(apartment, "REG003");
        activeAuthorizationsAre();

        PlateLookupResponse response = service.lookupPlate(PROPERTY_ID, "REG003");

        assertThat(response.status()).isEqualTo(PlateStatus.RESIDENT_REGISTERED);
        assertThat(response.vehicle().make()).isEqualTo("Toyota");
    }

    @Test
    void searchedPlateIsNormalizedLikeOnSave() {
        propertyExists();
        Apartment apartment = apartment("301");
        carIsRegistered(apartment, "AUTH001");
        activeAuthorizationsAre();

        // Typed messy, must still find the stored "AUTH001"
        PlateLookupResponse response = service.lookupPlate(PROPERTY_ID, "auth-001");

        assertThat(response.plate()).isEqualTo("AUTH001");
        assertThat(response.status()).isEqualTo(PlateStatus.RESIDENT_REGISTERED);
    }

    @Test
    void activeCheckUsesTheHoustonDateNotUtc() {
        propertyExists();
        Apartment apartment = apartment("301");
        carIsRegistered(apartment, "AUTH001");
        activeAuthorizationsAre();

        service.lookupPlate(PROPERTY_ID, "AUTH001");

        // UTC already says Jan 1. The query must receive Dec 31
        verify(authorizationRepository).findActiveForApartment(any(), eq(LocalDate.of(2026, 12, 31)));
        assertThat(service.lookupPlate(PROPERTY_ID, "AUTH001").checkedAt().getOffset().toString()).isEqualTo("-06:00");
    }

    @Test
    void emptyPlateIsRejected() {
        propertyExists();

        assertThatThrownBy(() -> service.lookupPlate(PROPERTY_ID, "---"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void unknownPropertyFailsInsteadOfSayingUnknownForEveryPlate() {
        when(propertyRepository.existsById(PROPERTY_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.lookupPlate(PROPERTY_ID, "AUTH001"))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
        verifyNoInteractions(vehicleRepository);
    }

    // ---------- Spot lookup ----------

    @Test
    void assignedSpotListsEveryCarOfTheApartment() {
        propertyExists();
        Apartment apartment = apartment("301");
        ParkingSpot spot = spot("R1");
        when(parkingSpotRepository.findByPropertyIdAndLabel(PROPERTY_ID, "R1")).thenReturn(Optional.of(spot));
        when(authorizationRepository.findActiveForSpot(any(), eq(TODAY)))
                .thenReturn(List.of(reserved(apartment, "R1", TODAY)));
        when(vehicleRepository.findByApartmentId(any()))
                .thenReturn(List.of(vehicle(apartment, "AUTH001"), vehicle(apartment, "AUTH004")));

        // Label typed messy on purpose
        SpotLookupResponse response = service.lookupSpot(PROPERTY_ID, "r-1");

        assertThat(response.assigned()).isTrue();
        assertThat(response.unitNumber()).isEqualTo("301");
        assertThat(response.vehicles()).extracting("plate").containsExactly("AUTH001", "AUTH004");
    }

    @Test
    void unassignedSpotSaysSo() {
        propertyExists();
        when(parkingSpotRepository.findByPropertyIdAndLabel(PROPERTY_ID, "R2")).thenReturn(Optional.of(spot("R2")));
        when(authorizationRepository.findActiveForSpot(any(), eq(TODAY))).thenReturn(List.of());

        SpotLookupResponse response = service.lookupSpot(PROPERTY_ID, "R2");

        assertThat(response.assigned()).isFalse();
        assertThat(response.vehicles()).isEmpty();
    }

    // ---------- Helpers to build test data ----------

    private void propertyExists() {
        when(propertyRepository.existsById(PROPERTY_ID)).thenReturn(true);
    }

    private void carIsRegistered(Apartment apartment, String plate) {
        when(vehicleRepository.findByPropertyIdAndPlate(PROPERTY_ID, plate))
                .thenReturn(Optional.of(vehicle(apartment, plate)));
    }

    // eq(TODAY) makes the test fail if the service ever asks with the wrong date
    private void activeAuthorizationsAre(ParkingAuthorization... authorizations) {
        when(authorizationRepository.findActiveForApartment(any(), eq(TODAY)))
                .thenReturn(List.of(authorizations));
    }

    private Apartment apartment(String unitNumber) {
        Apartment apartment = new Apartment();
        apartment.setUnitNumber(unitNumber);
        return apartment;
    }

    private Vehicle vehicle(Apartment apartment, String plate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(plate);
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setYear(2020);
        vehicle.setColor("White");
        vehicle.setApartment(apartment);
        return vehicle;
    }

    private ParkingSpot spot(String label) {
        ParkingSpot spot = new ParkingSpot();
        spot.setLabel(label);
        return spot;
    }

    private ParkingAuthorization permit(Apartment apartment, LocalDate validUntil) {
        ParkingAuthorization authorization = new ParkingAuthorization();
        authorization.setApartment(apartment);
        authorization.setValidUntil(validUntil);
        return authorization;
    }

    private ParkingAuthorization reserved(Apartment apartment, String label, LocalDate validUntil) {
        ParkingAuthorization authorization = permit(apartment, validUntil);
        authorization.setSpot(spot(label));
        return authorization;
    }
}