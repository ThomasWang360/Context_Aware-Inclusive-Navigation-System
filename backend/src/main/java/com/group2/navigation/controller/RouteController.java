package com.group2.navigation.controller;

import com.group2.navigation.model.RouteRequest;
import com.group2.navigation.model.RouteResponse;
import com.group2.navigation.model.UserPreferences;
import com.group2.navigation.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
     * Calculate a route based on start/end coordinates and preferences.
     * 
     * POST /api/route
     * Body: {
     *   "startLat": 43.6532,
     *   "startLng": -79.3832,
     *   "endLat": 43.6629,
     *   "endLng": -79.3957,
     *   "preferences": {
     *     "wheelchairAccessible": true,
     *     "avoidHighCrime": true,
     *     ...
     *   }
     * }
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
     * Get nearby points of interest.
     * 
     * GET /api/pois?lat=43.65&lng=-79.38&radius=500&type=hospital
     * 
     * TODO (Simon, Thomas): Implement this endpoint
     */
    @GetMapping("/pois")
    public ResponseEntity<Object> getNearbyPOIs(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "500") double radius,
            @RequestParam(required = false) String type) {
        
        // TODO: Implement POI lookup from database
        return ResponseEntity.ok(Map.of(
            "message", "TODO: Implement POI lookup",
            "lat", lat,
            "lng", lng,
            "radius", radius,
            "type", type
        ));
    }
}
