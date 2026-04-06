package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UpdateUserCredentialsRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validEmail_noViolations() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setEmail("test@example.com");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void validPassword_noViolations() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setPassword("newpassword123");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void bothFields_noViolations() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setEmail("user@example.com");
        req.setPassword("securepass1");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void emailTooLong_violation() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setEmail("a".repeat(256));
        Set<ConstraintViolation<UpdateUserCredentialsRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("255");
    }

    @Test
    void passwordTooShort_violation() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setPassword("short");
        Set<ConstraintViolation<UpdateUserCredentialsRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void passwordTooLong_violation() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setPassword("p".repeat(101));
        Set<ConstraintViolation<UpdateUserCredentialsRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void passwordMinLength_noViolation() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setPassword("12345678");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void passwordMaxLength_noViolation() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setPassword("p".repeat(100));
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void bothNull_noViolations() {
        // Both fields are optional per validation annotations
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void gettersSetters_work() {
        UpdateUserCredentialsRequest req = new UpdateUserCredentialsRequest();
        req.setEmail("test@test.com");
        req.setPassword("password123");
        assertThat(req.getEmail()).isEqualTo("test@test.com");
        assertThat(req.getPassword()).isEqualTo("password123");
    }
}
