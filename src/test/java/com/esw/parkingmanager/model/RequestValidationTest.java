package com.esw.parkingmanager.model;

import com.esw.parkingmanager.dto.CreateUserRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RequestValidationTest {

    // The same validator Spring runs on @Valid, without starting Spring
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void completeVehicleIsValid() {
        assertThat(validator.validate(vehicle())).isEmpty();
    }

    @Test
    void vehicleWithoutMakeIsRejected() {
        Vehicle vehicle = vehicle();
        vehicle.setMake("   ");
        assertThat(invalidFields(validator.validate(vehicle))).containsExactly("make");
    }

    @Test
    void vehicleYearMustBeRealistic() {
        Vehicle vehicle = vehicle();
        vehicle.setYear(20219);
        assertThat(invalidFields(validator.validate(vehicle))).containsExactly("year");
    }

    @Test
    void missingVehicleYearIsRejected() {
        Vehicle vehicle = vehicle();
        vehicle.setYear(null);
        assertThat(invalidFields(validator.validate(vehicle))).containsExactly("year");
    }

    @Test
    void apartmentWithoutLimitIsRejectedNotZero() {
        Apartment apartment = new Apartment();
        apartment.setUnitNumber("301");
        apartment.setMaxVisitorPasses(2);
        // maxVehicles never set, like a JSON body that forgot it
        assertThat(invalidFields(validator.validate(apartment))).containsExactly("maxVehicles");
    }

    @Test
    void negativeLimitIsRejected() {
        Apartment apartment = new Apartment();
        apartment.setUnitNumber("301");
        apartment.setMaxVehicles(-3);
        apartment.setMaxVisitorPasses(2);
        assertThat(invalidFields(validator.validate(apartment))).containsExactly("maxVehicles");
    }

    @Test
    void userEmailMustLookLikeAnEmail() {
        CreateUserRequest request = new CreateUserRequest("not-an-email", "a-long-temporary-pass",
                Role.TOWING_OPERATOR, null, null);
        assertThat(invalidFields(validator.validate(request))).containsExactly("email");
    }

    private Vehicle vehicle() {
        Vehicle vehicle = new Vehicle();
        vehicle.setPlate("ABC123");
        vehicle.setMake("Toyota");
        vehicle.setModel("Corolla");
        vehicle.setYear(2020);
        vehicle.setColor("White");
        return vehicle;
    }

    private <T> Set<String> invalidFields(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}