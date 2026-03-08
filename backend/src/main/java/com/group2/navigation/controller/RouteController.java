package com.group2.navigation.controller;

import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.model.*;
import com.group2.navigation.repository.ConstructionProjectRepository;
import com.group2.navigation.repository.HealthServiceRepository;
import com.group2.navigation.service.GeocodingService;
import com.group2.navigation.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API endpoints for route calculation.
 * 
 * TODO (Simon - lead, Thomas):
 * - Add input validation
 * - Add error handling
 * - Add rate limiting if needed
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Allow Appsmith to call this API
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
     * Body (coordinates):
     *   { "startLat": 43.6532, "startLng": -79.3832,
     *     "endLat": 43.6629, "endLng": -79.3957, "preferences": {...} }
     *
     * Body (addresses):
     *   { "startAddress": "CN Tower",
     *     "endAddress": "University of Toronto",
     *     "preferences": {...} }
     */
    @PostMapping("/route")
    public ResponseEntity<RouteResponse> calculateRoute(@RequestBody RouteRequest request) {
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
    public ResponseEntity<Object> geocode(@RequestParam String address) {
        double[] coords = geocodingService.geocode(address);
        if (coords == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Address not found: " + address));
        }
        return ResponseEntity.ok(Map.of(
                "lat", coords[0],
                "lng", coords[1],
                "address", address,
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
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") double radius,
            @RequestParam(required = false) String type) {

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
}
