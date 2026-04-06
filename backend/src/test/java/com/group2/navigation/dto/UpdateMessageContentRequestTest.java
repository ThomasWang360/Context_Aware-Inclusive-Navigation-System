package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UpdateMessageContentRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private UpdateMessageContentRequest valid() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("Updated message content");
        return req;
    }

    @Test
    void validRequest_noViolations() {
        assertThat(validator.validate(valid())).isEmpty();
    }

    @Test
    void blankContent_violation() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("");
        Set<ConstraintViolation<UpdateMessageContentRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("content is required");
    }

    @Test
    void nullContent_violation() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        Set<ConstraintViolation<UpdateMessageContentRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void contentMaxLength_noViolation() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("x".repeat(4000));
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void contentTooLong_violation() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("x".repeat(4001));
        Set<ConstraintViolation<UpdateMessageContentRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).contains("4000");
    }

    @Test
    void whitespaceOnlyContent_violation() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("   ");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void getterSetter_works() {
        UpdateMessageContentRequest req = new UpdateMessageContentRequest();
        req.setContent("test");
        assertThat(req.getContent()).isEqualTo("test");
    }
}
