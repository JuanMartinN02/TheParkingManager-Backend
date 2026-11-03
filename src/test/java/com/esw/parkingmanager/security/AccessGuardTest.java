package com.esw.parkingmanager.security;

import com.esw.parkingmanager.model.Apartment;
import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.model.Role;
import com.esw.parkingmanager.repository.AppUserRepository;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccessGuardTest {

    private static final UUID PROPERTY_A = UUID.randomUUID();
    private static final UUID PROPERTY_B = UUID.randomUUID();
    private static final UUID APARTMENT_301 = UUID.randomUUID();
    private static final UUID APARTMENT_302 = UUID.randomUUID();

    @Mock private AppUserRepository userRepository;

    private AccessGuard access;

    @BeforeEach
    void setUp() {
        access = new AccessGuard(userRepository);
    }

    // The logged in user is stored per thread, so it has to be cleared or it leaks into the next test
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void superAdminCanAccessAnyProperty() {
        logIn(user(Role.SUPER_ADMIN, null, null));
        assertThatCode(() -> access.checkProperty(PROPERTY_B, ADMINS)).doesNotThrowAnyException();
    }

    @Test
    void operatorCanUseStaffEndpointsOnAnyProperty() {
        logIn(user(Role.TOWING_OPERATOR, null, null));
        assertThatCode(() -> access.checkProperty(PROPERTY_A, STAFF)).doesNotThrowAnyException();
        assertThatCode(() -> access.checkProperty(PROPERTY_B, STAFF)).doesNotThrowAnyException();
    }

    @Test
    void operatorCannotUseAdminEndpoints() {
        logIn(user(Role.TOWING_OPERATOR, null, null));
        assertStatus(() -> access.checkProperty(PROPERTY_A, ADMINS), HttpStatus.FORBIDDEN);
    }

    @Test
    void propertyAdminCanAccessOwnProperty() {
        logIn(user(Role.PROPERTY_ADMIN, PROPERTY_A, null));
        assertThatCode(() -> access.checkProperty(PROPERTY_A, ADMINS)).doesNotThrowAnyException();
    }

    @Test
    void propertyAdminCannotAccessAnotherProperty() {
        logIn(user(Role.PROPERTY_ADMIN, PROPERTY_A, null));
        assertStatus(() -> access.checkProperty(PROPERTY_B, ADMINS), HttpStatus.FORBIDDEN);
    }

    @Test
    void residentCanAccessOwnApartment() {
        logIn(user(Role.RESIDENT, PROPERTY_A, APARTMENT_301));
        assertThatCode(() -> access.checkApartment(PROPERTY_A, APARTMENT_301, EVERYONE)).doesNotThrowAnyException();
    }

    @Test
    void residentCannotAccessNeighborApartment() {
        // The IDOR case: same property, logged in, just a different id in the URL
        logIn(user(Role.RESIDENT, PROPERTY_A, APARTMENT_301));
        assertStatus(() -> access.checkApartment(PROPERTY_A, APARTMENT_302, EVERYONE), HttpStatus.FORBIDDEN);
    }

    @Test
    void residentCannotUseAdminEndpointsInOwnApartment() {
        // Right apartment, wrong role, e.g. giving themself a reserved spot
        logIn(user(Role.RESIDENT, PROPERTY_A, APARTMENT_301));
        assertStatus(() -> access.checkApartment(PROPERTY_A, APARTMENT_301, ADMINS), HttpStatus.FORBIDDEN);
    }

    @Test
    void deactivatedUserIsRejected() {
        AppUser user = user(Role.PROPERTY_ADMIN, PROPERTY_A, null);
        user.setActive(false);
        logIn(user);
        assertStatus(() -> access.checkProperty(PROPERTY_A, ADMINS), HttpStatus.UNAUTHORIZED);
    }

    @Test
    void temporaryPasswordBlocksEverything() {
        AppUser user = user(Role.SUPER_ADMIN, null, null);
        user.setMustChangePassword(true);
        logIn(user);
        assertStatus(() -> access.checkRole(SUPER_ADMIN_ONLY), HttpStatus.FORBIDDEN);
        // ...except the endpoints needed to change it
        assertThatCode(() -> access.currentUserIgnoringPasswordChange()).doesNotThrowAnyException();
    }

    @Test
    void notLoggedInIsRejected() {
        assertStatus(() -> access.checkRole(EVERYONE), HttpStatus.UNAUTHORIZED);
    }

    // ---------- Helpers ----------

    // Puts this user in the security context, the same place Spring puts them after a real login
    private void logIn(AppUser user) {
        AuthenticatedUser principal = new AuthenticatedUser(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        // AccessGuard reloads the user from the database on every check
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    }

    private void assertStatus(ThrowingCallable call, HttpStatus status) {
        assertThatThrownBy(call).isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }

    private AppUser user(Role role, UUID propertyId, UUID apartmentId) {
        AppUser user = new AppUser();
        // The real code has no setId on purpose, so tests set ids through reflection
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setEmail(role.name().toLowerCase() + "@test.com");
        user.setPasswordHash("hash");
        user.setRole(role);
        // New accounts default to true, tests start from a normal logged in user
        user.setMustChangePassword(false);
        if (propertyId != null) {
            user.setProperty(property(propertyId));
        }
        if (apartmentId != null) {
            user.setApartment(apartment(apartmentId, propertyId));
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