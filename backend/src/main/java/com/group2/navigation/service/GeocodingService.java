package com.group2.navigation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Converts street addresses to lat/lng coordinates using OpenStreetMap's
 * Nominatim geocoding API.  Results are biased toward the Toronto area.
 *
 * Nominatim usage policy requires at most 1 request per second and a
 * descriptive User-Agent header; both are enforced here.
 */
@Service
public class GeocodingService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    // Bias results to the Greater Toronto Area (west-lng, north-lat, east-lng, south-lat)
    private static final String VIEWBOX = "-79.65,43.85,-79.10,43.58";

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private long lastRequestTime = 0;

    public GeocodingService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Geocode an address string to [lat, lng].
     *
     * @return double[] {lat, lng}, or null if the address could not be resolved
     */
    public double[] geocode(String address) {
        if (address == null || address.isBlank()) return null;

        // Append "Toronto" if the query doesn't already mention it,
        // to improve hit rate for short/ambiguous addresses
        String query = address.strip();
        if (!query.toLowerCase().contains("toronto")) {
            query = query + ", Toronto, ON";
        }

        throttle();

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = NOMINATIM_URL
                    + "?q=" + encoded
                    + "&format=json&limit=1"
                    + "&bounded=1&viewbox=" + VIEWBOX;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "MIE350-ContextAwareNav/1.0")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) return null;

            JsonNode results = mapper.readTree(response.body());
            if (!results.isArray() || results.isEmpty()) return null;

            JsonNode first = results.get(0);
            double lat = Double.parseDouble(first.get("lat").asText());
            double lng = Double.parseDouble(first.get("lon").asText());

            System.out.println("[Geocode] \"" + address + "\" -> " + lat + ", " + lng);
            return new double[]{lat, lng};

        } catch (Exception e) {
            System.err.println("[Geocode] Error for \"" + address + "\": " + e.getMessage());
            return null;
        }
    }

    /** Enforce Nominatim's 1-request-per-second policy. */
    private synchronized void throttle() {
        long now = System.currentTimeMillis();
        long wait = 1000 - (now - lastRequestTime);
        if (wait > 0) {
            try { Thread.sleep(wait); } catch (InterruptedException ignored) {}
        }
        lastRequestTime = System.currentTimeMillis();
    }
}
