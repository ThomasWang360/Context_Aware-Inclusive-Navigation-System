package com.group2.navigation.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class UserPreferencesTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void defaults_allZeroExceptTimeOfDay() {
        UserPreferences prefs = new UserPreferences();
        assertThat(prefs.getWheelchairWeight()).isEqualTo(0);
        assertThat(prefs.getCrimeWeight()).isEqualTo(0);
        assertThat(prefs.getLightingWeight()).isEqualTo(0);
        assertThat(prefs.getConstructionWeight()).isEqualTo(0);
        assertThat(prefs.getTimeOfDay()).isEqualTo(12);
        assertThat(prefs.getMaxDistanceToHospital()).isEqualTo(0);
    }

    @Test
    void validPreferences_noViolations() {
        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(5.0);
        prefs.setCrimeWeight(8.0);
        prefs.setLightingWeight(3.0);
        prefs.setConstructionWeight(10.0);
        prefs.setTimeOfDay(22);
        prefs.setMaxDistanceToHospital(5000);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isEmpty();
    }

    @Test
    void allWeightsAtBoundaries_valid() {
        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(0);
        prefs.setCrimeWeight(10.0);
        prefs.setLightingWeight(0);
        prefs.setConstructionWeight(10.0);
        prefs.setTimeOfDay(0);
        prefs.setMaxDistanceToHospital(50000);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isEmpty();
    }

    @Test
    void wheelchairWeight_aboveMax_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(11.0);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Wheelchair weight must be at most 10"))).isTrue();
    }

    @Test
    void wheelchairWeight_negative_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(-1.0);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void crimeWeight_aboveMax_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setCrimeWeight(15.0);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void lightingWeight_negative_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(-0.1);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void constructionWeight_aboveMax_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setConstructionWeight(10.1);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void timeOfDay_tooHigh_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setTimeOfDay(24);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
        assertThat(violations.stream().anyMatch(v ->
                v.getMessage().contains("Time of day must be at most 23"))).isTrue();
    }

    @Test
    void timeOfDay_negative_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setTimeOfDay(-1);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void maxDistanceToHospital_tooHigh_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setMaxDistanceToHospital(60000);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void maxDistanceToHospital_negative_violation() {
        UserPreferences prefs = new UserPreferences();
        prefs.setMaxDistanceToHospital(-100);

        Set<ConstraintViolation<UserPreferences>> violations = validator.validate(prefs);
        assertThat(violations).isNotEmpty();
    }

    @Test
    void toPreferences_roundTrip_viaUser() {
        User user = new User("test", "hash", "Test User");
        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(7.5);
        prefs.setCrimeWeight(3.0);
        prefs.setLightingWeight(9.0);
        prefs.setConstructionWeight(1.0);
        prefs.setTimeOfDay(5);
        prefs.setMaxDistanceToHospital(1500);

        user.applyPreferences(prefs);
        UserPreferences back = user.toPreferences();

        assertThat(back.getWheelchairWeight()).isEqualTo(7.5);
        assertThat(back.getCrimeWeight()).isEqualTo(3.0);
        assertThat(back.getLightingWeight()).isEqualTo(9.0);
        assertThat(back.getConstructionWeight()).isEqualTo(1.0);
        assertThat(back.getTimeOfDay()).isEqualTo(5);
        assertThat(back.getMaxDistanceToHospital()).isEqualTo(1500);
    }
}
