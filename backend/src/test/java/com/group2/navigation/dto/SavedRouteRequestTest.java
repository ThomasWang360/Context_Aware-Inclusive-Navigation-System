package com.group2.navigation.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class SavedRouteRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private SavedRouteRequest validRequest() {
        SavedRouteRequest req = new SavedRouteRequest();
        req.setUserId(1L);
        req.setName("Test Route");
        req.setStartAddress("CN Tower");
        req.setEndAddress("Union Station");
        req.setStartLat(43.6426);
        req.setStartLng(-79.3871);
        req.setEndLat(43.6453);
        req.setEndLng(-79.3806);
        req.setWheelchairWeight(5);
        req.setCrimeWeight(3);
        req.setLightingWeight(2);
        req.setConstructionWeight(1);
        req.setTimeOfDay(14);
        req.setMaxDistanceToHospital(3000);
        return req;
    }

    @Test
    void validRequest_noViolations() {
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    // --- userId ---

    @Test
    void nullUserId_violation() {
        SavedRouteRequest req = validRequest();
        req.setUserId(null);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("userId"));
    }

    @Test
    void zeroUserId_violation() {
        SavedRouteRequest req = validRequest();
        req.setUserId(0L);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    // --- name ---

    @Test
    void blankName_violation() {
        SavedRouteRequest req = validRequest();
        req.setName("");
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void nullName_violation() {
        SavedRouteRequest req = validRequest();
        req.setName(null);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void nameTooLong_violation() {
        SavedRouteRequest req = validRequest();
        req.setName("a".repeat(201));
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void nameExactly200_valid() {
        SavedRouteRequest req = validRequest();
        req.setName("a".repeat(200));
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    // --- addresses ---

    @Test
    void startAddressTooLong_violation() {
        SavedRouteRequest req = validRequest();
        req.setStartAddress("a".repeat(301));
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void endAddressTooLong_violation() {
        SavedRouteRequest req = validRequest();
        req.setEndAddress("a".repeat(301));
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void nullAddresses_valid() {
        SavedRouteRequest req = validRequest();
        req.setStartAddress(null);
        req.setEndAddress(null);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    // --- preference weights ---

    @Test
    void wheelchairWeightAboveMax_violation() {
        SavedRouteRequest req = validRequest();
        req.setWheelchairWeight(11);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void wheelchairWeightNegative_violation() {
        SavedRouteRequest req = validRequest();
        req.setWheelchairWeight(-1);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void crimeWeightAboveMax_violation() {
        SavedRouteRequest req = validRequest();
        req.setCrimeWeight(11);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void lightingWeightNegative_violation() {
        SavedRouteRequest req = validRequest();
        req.setLightingWeight(-1);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void constructionWeightAboveMax_violation() {
        SavedRouteRequest req = validRequest();
        req.setConstructionWeight(11);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void allWeightsAtBoundaries_valid() {
        SavedRouteRequest req = validRequest();
        req.setWheelchairWeight(0);
        req.setCrimeWeight(10);
        req.setLightingWeight(0);
        req.setConstructionWeight(10);
        req.setTimeOfDay(0);
        req.setMaxDistanceToHospital(0);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void allWeightsAtMaxBoundaries_valid() {
        SavedRouteRequest req = validRequest();
        req.setWheelchairWeight(10);
        req.setCrimeWeight(10);
        req.setLightingWeight(10);
        req.setConstructionWeight(10);
        req.setTimeOfDay(23);
        req.setMaxDistanceToHospital(50000);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    // --- timeOfDay ---

    @Test
    void timeOfDayTooHigh_violation() {
        SavedRouteRequest req = validRequest();
        req.setTimeOfDay(24);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void timeOfDayNegative_violation() {
        SavedRouteRequest req = validRequest();
        req.setTimeOfDay(-1);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    // --- maxDistanceToHospital ---

    @Test
    void maxDistanceToHospitalTooHigh_violation() {
        SavedRouteRequest req = validRequest();
        req.setMaxDistanceToHospital(50001);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void maxDistanceToHospitalNegative_violation() {
        SavedRouteRequest req = validRequest();
        req.setMaxDistanceToHospital(-1);
        Set<ConstraintViolation<SavedRouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }
}
