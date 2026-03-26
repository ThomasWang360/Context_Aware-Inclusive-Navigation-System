package com.group2.navigation.controller;

import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.dto.CreateHealthServiceRequest;
import com.group2.navigation.model.*;
import com.group2.navigation.repository.ConstructionProjectRepository;
import com.group2.navigation.repository.HealthServiceRepository;
import com.group2.navigation.service.GeocodingService;
import com.group2.navigation.service.RouteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
@Validated
public class RouteController {

    @Autowired
    private RouteService routeService;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private HealthServiceRepository healthServiceRepo;

    @Autowired
    private ConstructionProjectRepository constructionRepo;

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ok",
            "service", "navigation-api"
        ));
    }

    /**
     * Calculate a route.  Accepts coordinates OR street addresses (or a mix).
     *
     * POST /api/route
     */
    @PostMapping("/route")
    public ResponseEntity<RouteResponse> calculateRoute(@Valid @RequestBody RouteRequest request) {
        try {
            RouteResponse response = routeService.calculateRoute(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(RouteResponse.error("Error calculating route: " + e.getMessage()));
        }
    }

    /**
     * Get default preferences.
     *
     * GET /api/preferences
     */
    @GetMapping("/preferences")
    public ResponseEntity<UserPreferences> getDefaultPreferences() {
        return ResponseEntity.ok(new UserPreferences());
    }

    /**
     * Geocode a street address to lat/lng coordinates.
     *
     * GET /api/geocode?address=CN Tower
     */
    @GetMapping("/geocode")
    public ResponseEntity<Object> geocode(
            @RequestParam
            @NotBlank(message = "Address must not be blank")
            @Size(max = 200, message = "Address must not exceed 200 characters")
            String address) {

        String sanitized = address.replaceAll("[<>\"'&;]", "").trim();
        double[] coords = geocodingService.geocode(sanitized);
        if (coords == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Address not found: " + sanitized));
        }
        return ResponseEntity.ok(Map.of(
                "lat", coords[0],
                "lng", coords[1],
                "address", sanitized,
                "success", true));
    }

    /**
     * Get nearby points of interest (hospitals, construction zones).
     *
     * GET /api/pois?lat=43.65&lng=-79.38&radius=500
     * GET /api/pois?lat=43.65&lng=-79.38&radius=1000&type=hospital
     * GET /api/pois?lat=43.65&lng=-79.38&radius=500&type=construction
     */
    @GetMapping("/pois")
    public ResponseEntity<Object> getNearbyPOIs(
            @RequestParam
            @DecimalMin(value = "-90", message = "Latitude must be between -90 and 90")
            @DecimalMax(value = "90", message = "Latitude must be between -90 and 90")
            double lat,

            @RequestParam
            @DecimalMin(value = "-180", message = "Longitude must be between -180 and 180")
            @DecimalMax(value = "180", message = "Longitude must be between -180 and 180")
            double lng,

            @RequestParam(defaultValue = "500")
            @DecimalMin(value = "1", message = "Radius must be at least 1 meter")
            @DecimalMax(value = "10000", message = "Radius must not exceed 10000 meters")
            double radius,

            @RequestParam(required = false) String type) {

        if (type != null && !type.isBlank()
                && !"hospital".equalsIgnoreCase(type)
                && !"construction".equalsIgnoreCase(type)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid POI type. Allowed values: hospital, construction"));
        }

        double latOffset = radius / 111_000.0;
        double lngOffset = radius / (111_000.0 * Math.cos(Math.toRadians(lat)));
        double minLat = lat - latOffset, maxLat = lat + latOffset;
        double minLng = lng - lngOffset, maxLng = lng + lngOffset;

        List<Map<String, Object>> pois = new ArrayList<>();

        if (type == null || "hospital".equalsIgnoreCase(type)) {
            List<HealthService> hospitals =
                    healthServiceRepo.findInBoundingBox(minLat, maxLat, minLng, maxLng);
            for (HealthService h : hospitals) {
                Map<String, Object> poi = new LinkedHashMap<>();
                poi.put("type", "hospital");
                poi.put("name", h.getAgencyName());
                poi.put("address", h.getAddress());
                poi.put("lat", h.getLatitude());
                poi.put("lng", h.getLongitude());
                poi.put("phone", h.getPhone());
                poi.put("accessibility", h.getAccessibility());
                poi.put("distanceMeters",
                        Math.round(Graph.haversineDistance(lat, lng,
                                h.getLatitude(), h.getLongitude())));
                pois.add(poi);
            }
        }

        if (type == null || "construction".equalsIgnoreCase(type)) {
            List<ConstructionProject> projects =
                    constructionRepo.findActiveInBoundingBox(minLat, maxLat, minLng, maxLng);
            for (ConstructionProject c : projects) {
                Map<String, Object> poi = new LinkedHashMap<>();
                poi.put("type", "construction");
                poi.put("name", c.getProjectType());
                poi.put("address", c.getLocation());
                poi.put("lat", c.getLatitude());
                poi.put("lng", c.getLongitude());
                poi.put("status", c.getStatus());
                poi.put("duration", c.getDuration());
                poi.put("distanceMeters",
                        Math.round(Graph.haversineDistance(lat, lng,
                                c.getLatitude(), c.getLongitude())));
                pois.add(poi);
            }
        }

        pois.sort(Comparator.comparingLong(a -> (long) a.get("distanceMeters")));

        return ResponseEntity.ok(Map.of(
                "pois", pois,
                "count", pois.size(),
                "center", Map.of("lat", lat, "lng", lng),
                "radiusMeters", radius
        ));
    }

    /**
     * Add a health service POI.
     *
     * POST /api/pois/health
     */
    @PostMapping("/pois/health")
    public ResponseEntity<HealthService> createHealthPoi(@Valid @RequestBody CreateHealthServiceRequest body) {
        HealthService h = new HealthService(
                body.getAgencyName().trim(),
                body.getAddress() != null ? body.getAddress().trim() : null,
                body.getLatitude(),
                body.getLongitude(),
                body.getAccessibility() != null ? body.getAccessibility().trim() : null,
                body.getPhone() != null ? body.getPhone().trim() : null);
        HealthService saved = healthServiceRepo.save(h);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Delete a health service by id.
     *
     * DELETE /api/pois/health/{id}
     */
    @DeleteMapping("/pois/health/{id}")
    public ResponseEntity<Void> deleteHealthPoi(
            @PathVariable @Min(value = 1, message = "id must be a positive number") Long id) {
        healthServiceRepo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
