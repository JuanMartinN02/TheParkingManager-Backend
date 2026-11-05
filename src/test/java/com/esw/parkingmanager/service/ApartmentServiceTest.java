package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.MoveOutResponse;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.ApartmentStatus;
import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    private static final UUID PROPERTY_A = UUID.randomUUID();
    private static final UUID APARTMENT_301 = UUID.randomUUID();

    @Mock private ApartmentRepository apartmentRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private ParkingAuthorizationRepository authorizationRepository;
    @Mock private AppUserRepository userRepository;

    private ApartmentService service;

    @BeforeEach
    void setUp() {
        service = new ApartmentService(apartmentRepository, propertyRepository, vehicleRepository,
                authorizationRepository, userRepository);
    }

    @Test
    void moveOutClearsTheApartment() {
        Apartment apartment = apartmentWithStatus(ApartmentStatus.ACTIVE);
        AppUser resident1 = resident();
        AppUser resident2 = resident();
        when(authorizationRepository.deleteByApartmentId(APARTMENT_301)).thenReturn(2L);
        when(vehicleRepository.deleteByApartmentId(APARTMENT_301)).thenReturn(3L);
        when(userRepository.findByApartmentIdAndActiveTrue(APARTMENT_301)).thenReturn(List.of(resident1, resident2));

        MoveOutResponse response = service.moveOut(PROPERTY_A, APARTMENT_301);

        assertThat(apartment.getStatus()).isEqualTo(ApartmentStatus.MOVED_OUT);
        assertThat(resident1.isActive()).isFalse();
        assertThat(resident2.isActive()).isFalse();
        assertThat(response.authorizationsRemoved()).isEqualTo(2);
        assertThat(response.vehiclesRemoved()).isEqualTo(3);
        assertThat(response.accountsDeactivated()).isEqualTo(2);
    }

    @Test
    void secondMoveOutIsRejectedAndDeletesNothing() {
        apartmentWithStatus(ApartmentStatus.MOVED_OUT);

        assertThatThrownBy(() -> service.moveOut(PROPERTY_A, APARTMENT_301))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        // Nothing may be touched when the move-out is refused
        verifyNoInteractions(vehicleRepository, authorizationRepository, userRepository);
    }

    @Test
    void moveInReactivatesTheApartment() {
        apartmentWithStatus(ApartmentStatus.MOVED_OUT);
        when(apartmentRepository.save(any(Apartment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(service.moveIn(PROPERTY_A, APARTMENT_301).getStatus()).isEqualTo(ApartmentStatus.ACTIVE);
    }

    @Test
    void moveInOnActiveApartmentIsRejected() {
        apartmentWithStatus(ApartmentStatus.ACTIVE);

        assertThatThrownBy(() -> service.moveIn(PROPERTY_A, APARTMENT_301))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    // ---------- Helpers ----------

    // Builds the apartment and makes the fake repository return it, like getOne finds it in the database
    private Apartment apartmentWithStatus(ApartmentStatus status) {
        Apartment apartment = new Apartment();
        ReflectionTestUtils.setField(apartment, "id", APARTMENT_301);
        apartment.setUnitNumber("301");
        apartment.setStatus(status);
        when(apartmentRepository.findByIdAndPropertyId(APARTMENT_301, PROPERTY_A)).thenReturn(Optional.of(apartment));
        return apartment;
    }

    private AppUser resident() {
        AppUser resident = new AppUser();
        resident.setRole(Role.RESIDENT);
        return resident;
    }
}