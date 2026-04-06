package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class CreateMessageRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private CreateMessageRequest valid() {
        CreateMessageRequest req = new CreateMessageRequest();
        req.setSenderId(1L);
        req.setReceiverId(2L);
        req.setContent("Hello!");
        return req;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void nullSenderId_violation() {
        CreateMessageRequest req = valid();
        req.setSenderId(null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void nullReceiverId_violation() {
        CreateMessageRequest req = valid();
        req.setReceiverId(null);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void zeroSenderId_violation() {
        CreateMessageRequest req = valid();
        req.setSenderId(0L);
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void blankContent_violation() {
        CreateMessageRequest req = valid();
        req.setContent("");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void contentTooLong_violation() {
        CreateMessageRequest req = valid();
        req.setContent("x".repeat(4001));
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void contentExactly4000_valid() {
        CreateMessageRequest req = valid();
        req.setContent("x".repeat(4000));
        assertThat(validator.validate(req)).isEmpty();
    }
}
