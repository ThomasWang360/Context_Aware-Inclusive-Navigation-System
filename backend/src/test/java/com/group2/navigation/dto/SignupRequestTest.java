package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class SignupRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SignupRequest valid() {
        SignupRequest req = new SignupRequest();
        req.setUsername("testuser");
        req.setPassword("password123");
        req.setDisplayName("Test User");
        req.setLocation("Toronto");
        return req;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void blankUsername_violation() {
        SignupRequest req = valid();
        req.setUsername("");
        Set<ConstraintViolation<SignupRequest>> v = validator.validate(req);
        assertThat(v).isNotEmpty();
    }

    @Test
    void nullUsername_violation() {
        SignupRequest req = valid();
        req.setUsername(null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void usernameTooShort_violation() {
        SignupRequest req = valid();
        req.setUsername("ab");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void usernameTooLong_violation() {
        SignupRequest req = valid();
        req.setUsername("a".repeat(31));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void usernameSpecialChars_violation() {
        SignupRequest req = valid();
        req.setUsername("user@name!");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void usernameWithUnderscore_valid() {
        SignupRequest req = valid();
        req.setUsername("test_user");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void blankPassword_violation() {
        SignupRequest req = valid();
        req.setPassword("");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void passwordTooShort_violation() {
        SignupRequest req = valid();
        req.setPassword("short");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void passwordTooLong_violation() {
        SignupRequest req = valid();
        req.setPassword("p".repeat(101));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void password_exactly8Chars_valid() {
        SignupRequest req = valid();
        req.setPassword("12345678");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void displayNameTooLong_violation() {
        SignupRequest req = valid();
        req.setDisplayName("A".repeat(51));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void displayNameWithSpecialChars_violation() {
        SignupRequest req = valid();
        req.setDisplayName("<script>alert('xss')</script>");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void displayNameNull_valid() {
        SignupRequest req = valid();
        req.setDisplayName(null);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void locationTooLong_violation() {
        SignupRequest req = valid();
        req.setLocation("A".repeat(201));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void locationWithSpecialChars_violation() {
        SignupRequest req = valid();
        req.setLocation("Toronto; DROP TABLE users");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void locationNull_valid() {
        SignupRequest req = valid();
        req.setLocation(null);
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void locationWithCommasAndPeriods_valid() {
        SignupRequest req = valid();
        req.setLocation("Toronto, ON. Canada");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void displayNameWithApostrophe_valid() {
        SignupRequest req = valid();
        req.setDisplayName("O'Brien");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void displayNameWithHyphen_valid() {
        SignupRequest req = valid();
        req.setDisplayName("Mary-Jane");
        assertThat(validator.validate(req)).isEmpty();
    }
}
