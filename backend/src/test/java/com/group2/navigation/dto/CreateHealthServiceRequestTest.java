package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CreateHealthServiceRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CreateHealthServiceRequest valid() {
        CreateHealthServiceRequest req = new CreateHealthServiceRequest();
        req.setAgencyName("Downtown Hospital");
        req.setAddress("123 Main St");
        req.setLatitude(43.65);
        req.setLongitude(-79.38);
        req.setAccessibility("Wheelchair accessible");
        req.setPhone("416-555-1234");
        return req;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void blankAgencyName_violation() {
        CreateHealthServiceRequest req = valid();
        req.setAgencyName("");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void nullAgencyName_violation() {
        CreateHealthServiceRequest req = valid();
        req.setAgencyName(null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void agencyNameTooLong_violation() {
        CreateHealthServiceRequest req = valid();
        req.setAgencyName("x".repeat(501));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void latitudeTooHigh_violation() {
        CreateHealthServiceRequest req = valid();
        req.setLatitude(91);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void latitudeTooLow_violation() {
        CreateHealthServiceRequest req = valid();
        req.setLatitude(-91);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void longitudeTooHigh_violation() {
        CreateHealthServiceRequest req = valid();
        req.setLongitude(181);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void longitudeTooLow_violation() {
        CreateHealthServiceRequest req = valid();
        req.setLongitude(-181);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void addressTooLong_violation() {
        CreateHealthServiceRequest req = valid();
        req.setAddress("x".repeat(501));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void phoneTooLong_violation() {
        CreateHealthServiceRequest req = valid();
        req.setPhone("x".repeat(101));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void accessibilityTooLong_violation() {
        CreateHealthServiceRequest req = valid();
        req.setAccessibility("x".repeat(1001));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void nullOptionalFields_valid() {
        CreateHealthServiceRequest req = new CreateHealthServiceRequest();
        req.setAgencyName("Test");
        req.setLatitude(43.65);
        req.setLongitude(-79.38);
        assertThat(validator.validate(req)).isEmpty();
    }
}
