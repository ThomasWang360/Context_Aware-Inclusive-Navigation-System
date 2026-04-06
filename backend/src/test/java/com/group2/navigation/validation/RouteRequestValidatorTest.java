package com.group2.navigation.validation;

import com.group2.navigation.model.RouteRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class RouteRequestValidatorTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validTorontoCoordinates_passes() {
        RouteRequest req = new RouteRequest(43.6532, -79.3832, 43.6600, -79.3700);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void validAddresses_passes() {
        RouteRequest req = new RouteRequest();
        req.setStartAddress("CN Tower");
        req.setEndAddress("Union Station");
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void mixedCoordinatesAndAddress_passes() {
        RouteRequest req = new RouteRequest();
        req.setStartLat(43.6532);
        req.setStartLng(-79.3832);
        req.setEndAddress("Union Station");
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void noStartLocation_fails() {
        RouteRequest req = new RouteRequest();
        req.setEndLat(43.66);
        req.setEndLng(-79.37);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Start location"))).isTrue();
    }

    @Test
    void noEndLocation_fails() {
        RouteRequest req = new RouteRequest();
        req.setStartLat(43.65);
        req.setStartLng(-79.38);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("End location"))).isTrue();
    }

    @Test
    void neitherStartNorEnd_failsBoth() {
        RouteRequest req = new RouteRequest();
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void coordinatesOutsideToronto_needsAddress() {
        // New York coordinates — not in Toronto bounding box
        RouteRequest req = new RouteRequest(40.7128, -74.0060, 43.66, -79.37);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        // Start fails (NYC not in Toronto), end passes (Toronto coords)
        assertThat(violations).isNotEmpty();
    }

    @Test
    void coordinatesOutsideToronto_butHasAddress_passes() {
        RouteRequest req = new RouteRequest();
        req.setStartLat(40.7128); // NYC
        req.setStartLng(-74.0060);
        req.setStartAddress("CN Tower"); // Address overrides
        req.setEndLat(43.66);
        req.setEndLng(-79.37);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void blankAddress_doesNotCount() {
        RouteRequest req = new RouteRequest();
        req.setStartAddress("   ");
        req.setEndAddress("Union Station");
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void torontoBoundaryMinLat_passes() {
        RouteRequest req = new RouteRequest(43.58, -79.38, 43.66, -79.37);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void torontoBoundaryMaxLat_passes() {
        RouteRequest req = new RouteRequest(43.86, -79.38, 43.66, -79.37);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isEmpty();
    }

    @Test
    void justBelowTorontoBoundary_fails() {
        RouteRequest req = new RouteRequest(43.57, -79.38, 43.66, -79.37);
        Set<ConstraintViolation<RouteRequest>> violations = validator.validate(req);
        assertThat(violations).isNotEmpty();
    }
}
