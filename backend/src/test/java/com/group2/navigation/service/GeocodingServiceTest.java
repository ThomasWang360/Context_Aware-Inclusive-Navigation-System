package com.group2.navigation.service;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for GeocodingService.
 *
 * These tests call the real Nominatim API. They are intentionally conservative
 * to respect the 1-request-per-second rate limit. Each test validates a
 * different geocoding scenario within the Toronto area.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeocodingServiceTest {

    private final GeocodingService geocodingService = new GeocodingService();

    // --- successful geocoding ---

    @Test
    @Order(1)
    void geocode_knownLandmark_returnsCoordinates() {
        double[] coords = geocodingService.geocode("CN Tower");

        assertThat(coords).isNotNull();
        assertThat(coords).hasSize(2);
        // CN Tower is at approx 43.642, -79.387
        assertThat(coords[0]).isBetween(43.5, 43.8);
        assertThat(coords[1]).isBetween(-79.6, -79.1);
    }

    @Test
    @Order(2)
    void geocode_streetAddress_returnsTorontoCoordinates() {
        double[] coords = geocodingService.geocode("100 Queen St W, Toronto");

        assertThat(coords).isNotNull();
        assertThat(coords).hasSize(2);
        // Should be in Toronto bounds
        assertThat(coords[0]).isBetween(43.58, 43.86);
        assertThat(coords[1]).isBetween(-79.65, -79.10);
    }

    @Test
    @Order(3)
    void geocode_appendsToronto_forShortQuery() {
        // "Union Station" without "Toronto" should still resolve within Toronto
        // because the service appends ", Toronto, ON" to queries without "toronto"
        double[] coords = geocodingService.geocode("Union Station");

        assertThat(coords).isNotNull();
        assertThat(coords[0]).isBetween(43.58, 43.86);
        assertThat(coords[1]).isBetween(-79.65, -79.10);
    }

    // --- null/blank inputs ---

    @Test
    @Order(4)
    void geocode_nullAddress_returnsNull() {
        double[] coords = geocodingService.geocode(null);
        assertThat(coords).isNull();
    }

    @Test
    @Order(5)
    void geocode_blankAddress_returnsNull() {
        double[] coords = geocodingService.geocode("   ");
        assertThat(coords).isNull();
    }

    @Test
    @Order(6)
    void geocode_emptyAddress_returnsNull() {
        double[] coords = geocodingService.geocode("");
        assertThat(coords).isNull();
    }

    // --- nonexistent location ---

    @Test
    @Order(7)
    void geocode_nonsenseAddress_returnsNull() {
        double[] coords = geocodingService.geocode("zzzznonexistent99999xyz");
        assertThat(coords).isNull();
    }

    // --- address with special characters ---

    @Test
    @Order(8)
    void geocode_addressWithSpecialChars_handlesGracefully() {
        // Should not throw, even with unusual input
        double[] coords = geocodingService.geocode("Queen & Spadina, Toronto");
        // May or may not resolve, but should not throw
        if (coords != null) {
            assertThat(coords).hasSize(2);
        }
    }

    // --- throttling ---

    @Test
    @Order(9)
    void geocode_consecutiveCalls_respectsRateLimit() {
        long start = System.currentTimeMillis();

        geocodingService.geocode("King St W");
        geocodingService.geocode("Yonge St");

        long elapsed = System.currentTimeMillis() - start;
        // Should take at least ~1000ms due to throttling between calls
        assertThat(elapsed).isGreaterThanOrEqualTo(900L);
    }
}
