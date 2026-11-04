package com.esw.parkingmanager.service;

import com.esw.parkingmanager.dto.ChangePasswordRequest;
import com.esw.parkingmanager.dto.CreateUserRequest;
import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.repository.AppUserRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static com.esw.parkingmanager.model.Role.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID PROPERTY_A = UUID.randomUUID();
    private static final UUID PROPERTY_B = UUID.randomUUID();
    private static final UUID APARTMENT_301 = UUID.randomUUID();
    private static final String GOOD_PASSWORD = "a-long-temporary-pass";

    @Mock private AppUserRepository userRepository;
    @Mock private PropertyRepository propertyRepository;
    @Mock private ApartmentService apartmentService;

    // Real BCrypt, not a fake: hashing is part of what's tested. Cost 4 keeps the tests fast
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, propertyRepository, apartmentService, passwordEncoder);
    }

    // ---------- Creating users ----------

    @Test
    void propertyAdminCanCreateResidentInOwnProperty() {
        AppUser admin = user(PROPERTY_ADMIN, PROPERTY_A);
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(apartmentService.getOne(PROPERTY_A, APARTMENT_301)).thenReturn(apartment(APARTMENT_301, PROPERTY_A));
        savesWhatItGets();

        AppUser created = service.create(admin,
                new CreateUserRequest("  New@Test.com ", GOOD_PASSWORD, RESIDENT, PROPERTY_A, APARTMENT_301));

        assertThat(created.getEmail()).isEqualTo("new@test.com");
        assertThat(created.getProperty().getId()).isEqualTo(PROPERTY_A);
        assertThat(created.getApartment().getId()).isEqualTo(APARTMENT_301);
        assertThat(created.isMustChangePassword()).isTrue();
        // Stored as a hash, and the hash still matches the original password
        assertThat(created.getPasswordHash()).isNotEqualTo(GOOD_PASSWORD);
        assertThat(passwordEncoder.matches(GOOD_PASSWORD, created.getPasswordHash())).isTrue();
    }

    @Test
    void propertyAdminCannotCreateSuperAdmin() {
        AppUser admin = user(PROPERTY_ADMIN, PROPERTY_A);
        assertStatus(() -> service.create(admin,
                new CreateUserRequest("x@test.com", GOOD_PASSWORD, SUPER_ADMIN, null, null)), HttpStatus.FORBIDDEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void propertyAdminCannotCreateResidentInAnotherProperty() {
        AppUser admin = user(PROPERTY_ADMIN, PROPERTY_A);
        assertStatus(() -> service.create(admin,
                new CreateUserRequest("x@test.com", GOOD_PASSWORD, RESIDENT, PROPERTY_B, APARTMENT_301)), HttpStatus.FORBIDDEN);
        verify(userRepository, never()).save(any());
    }

    @Test
    void residentCannotCreateAnyone() {
        AppUser resident = user(RESIDENT, PROPERTY_A);
        assertStatus(() -> service.create(resident,
                new CreateUserRequest("x@test.com", GOOD_PASSWORD, RESIDENT, PROPERTY_A, APARTMENT_301)), HttpStatus.FORBIDDEN);
    }

    @Test
    void operatorCantBeLinkedToAProperty() {
        AppUser superAdmin = user(SUPER_ADMIN, null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        assertStatus(() -> service.create(superAdmin,
                new CreateUserRequest("op@test.com", GOOD_PASSWORD, TOWING_OPERATOR, PROPERTY_A, null)), HttpStatus.BAD_REQUEST);
        verify(userRepository, never()).save(any());
    }

    @Test
    void duplicateEmailIsRejected() {
        AppUser superAdmin = user(SUPER_ADMIN, null);
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);
        assertStatus(() -> service.create(superAdmin,
                new CreateUserRequest("Taken@test.com", GOOD_PASSWORD, TOWING_OPERATOR, null, null)), HttpStatus.CONFLICT);
    }

    @Test
    void shortPasswordIsRejected() {
        AppUser superAdmin = user(SUPER_ADMIN, null);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        assertStatus(() -> service.create(superAdmin,
                new CreateUserRequest("op@test.com", "short", TOWING_OPERATOR, null, null)), HttpStatus.BAD_REQUEST);
    }

    // ---------- Deactivating ----------

    @Test
    void nobodyCanDeactivateThemself() {
        AppUser superAdmin = user(SUPER_ADMIN, null);
        when(userRepository.findById(superAdmin.getId())).thenReturn(Optional.of(superAdmin));

        assertStatus(() -> service.deactivate(superAdmin, superAdmin.getId()), HttpStatus.CONFLICT);
        assertThat(superAdmin.isActive()).isTrue();
    }

    @Test
    void propertyAdminCannotDeactivateAnotherPropertyAdmin() {
        AppUser admin = user(PROPERTY_ADMIN, PROPERTY_A);
        AppUser otherAdmin = user(PROPERTY_ADMIN, PROPERTY_A);
        when(userRepository.findById(otherAdmin.getId())).thenReturn(Optional.of(otherAdmin));

        assertStatus(() -> service.deactivate(admin, otherAdmin.getId()), HttpStatus.FORBIDDEN);
        assertThat(otherAdmin.isActive()).isTrue();
    }

    @Test
    void propertyAdminCanDeactivateOwnResident() {
        AppUser admin = user(PROPERTY_ADMIN, PROPERTY_A);
        AppUser resident = user(RESIDENT, PROPERTY_A);
        when(userRepository.findById(resident.getId())).thenReturn(Optional.of(resident));
        savesWhatItGets();

        assertThat(service.deactivate(admin, resident.getId()).isActive()).isFalse();
    }

    // ---------- Changing password ----------

    @Test
    void changePasswordNeedsTheCurrentPassword() {
        AppUser resident = user(RESIDENT, PROPERTY_A);
        resident.setPasswordHash(passwordEncoder.encode(GOOD_PASSWORD));

        assertStatus(() -> service.changePassword(resident,
                new ChangePasswordRequest("wrong-password-123", "another-long-password")), HttpStatus.BAD_REQUEST);
    }

    @Test
    void changePasswordClearsTheTemporaryFlag() {
        AppUser resident = user(RESIDENT, PROPERTY_A);
        resident.setPasswordHash(passwordEncoder.encode(GOOD_PASSWORD));
        resident.setMustChangePassword(true);

        service.changePassword(resident, new ChangePasswordRequest(GOOD_PASSWORD, "brand-new-long-password"));

        assertThat(resident.isMustChangePassword()).isFalse();
        assertThat(passwordEncoder.matches("brand-new-long-password", resident.getPasswordHash())).isTrue();
    }

    // ---------- Helpers ----------

    // The fake save hands back the same object it receives, like the real one does
    private void savesWhatItGets() {
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertStatus(ThrowingCallable call, HttpStatus status) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }

    private AppUser user(Role role, UUID propertyId) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail(role.name().toLowerCase() + "@test.com");
        user.setRole(role);
        user.setMustChangePassword(false);
        if (propertyId != null) {
            user.setProperty(property(propertyId));
        }
        return user;
    }

    private Property property(UUID id) {
        Property property = new Property();
        ReflectionTestUtils.setField(property, "id", id);
        return property;
    }

    private Apartment apartment(UUID id, UUID propertyId) {
        Apartment apartment = new Apartment();
        ReflectionTestUtils.setField(apartment, "id", id);
        apartment.setProperty(property(propertyId));
        return apartment;
    }
}