package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private LoginRequest valid() {
        LoginRequest req = new LoginRequest();
        req.setUsername("testuser");
        req.setPassword("password123");
        return req;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void blankUsername_violation() {
        LoginRequest req = valid();
        req.setUsername("");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void nullUsername_violation() {
        LoginRequest req = valid();
        req.setUsername(null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void usernameTooShort_violation() {
        LoginRequest req = valid();
        req.setUsername("ab");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void usernameTooLong_violation() {
        LoginRequest req = valid();
        req.setUsername("a".repeat(31));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void blankPassword_violation() {
        LoginRequest req = valid();
        req.setPassword("");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void passwordTooShort_violation() {
        LoginRequest req = valid();
        req.setPassword("short");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void passwordTooLong_violation() {
        LoginRequest req = valid();
        req.setPassword("p".repeat(101));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void password_exactly8_valid() {
        LoginRequest req = valid();
        req.setPassword("12345678");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void username_exactly3_valid() {
        LoginRequest req = valid();
        req.setUsername("abc");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void username_exactly30_valid() {
        LoginRequest req = valid();
        req.setUsername("a".repeat(30));
        assertThat(validator.validate(req)).isEmpty();
    }
}
