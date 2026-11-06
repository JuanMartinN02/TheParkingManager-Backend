package com.esw.parkingmanager.model;

import com.esw.parkingmanager.dto.ApartmentRequest;
import com.esw.parkingmanager.dto.CreateUserRequest;
import com.esw.parkingmanager.dto.PropertyRequest;
import com.esw.parkingmanager.dto.VehicleRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    // The same validator Spring runs on @Valid, without starting Spring
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void completeVehicleIsValid() {
        assertThat(validator.validate(vehicle("Toyota", 2020))).isEmpty();
    }

    @Test
    void vehicleWithoutMakeIsRejected() {
        assertThat(invalidFields(validator.validate(vehicle("   ", 2020)))).containsExactly("make");
    }

    @Test
    void vehicleYearMustBeRealistic() {
        assertThat(invalidFields(validator.validate(vehicle("Toyota", 20219)))).containsExactly("year");
    }

    @Test
    void missingVehicleYearIsRejected() {
        assertThat(invalidFields(validator.validate(vehicle("Toyota", null)))).containsExactly("year");
    }

    @Test
    void apartmentWithoutLimitIsRejectedNotZero() {
        // maxVehicles missing, like a JSON body that forgot it
        ApartmentRequest request = new ApartmentRequest("301", null, 2);
        assertThat(invalidFields(validator.validate(request))).containsExactly("maxVehicles");
    }

    @Test
    void negativeLimitIsRejected() {
        ApartmentRequest request = new ApartmentRequest("301", -3, 2);
        assertThat(invalidFields(validator.validate(request))).containsExactly("maxVehicles");
    }

    @Test
    void propertyPassDurationMustBePositive() {
        PropertyRequest request = new PropertyRequest("Prop A", 10, 20, 0, null);
        assertThat(invalidFields(validator.validate(request))).containsExactly("visitorPassDurationHours");
    }

    @Test
    void userEmailMustLookLikeAnEmail() {
        CreateUserRequest request = new CreateUserRequest("not-an-email", "a-long-temporary-pass",
                Role.TOWING_OPERATOR, null, null);
        assertThat(invalidFields(validator.validate(request))).containsExactly("email");
    }

    // Records can't be changed after creation, so each test builds the one it needs
    private VehicleRequest vehicle(String make, Integer year) {
        return new VehicleRequest("ABC123", make, "Corolla", year, "White");
    }

    private <T> Set<String> invalidFields(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}