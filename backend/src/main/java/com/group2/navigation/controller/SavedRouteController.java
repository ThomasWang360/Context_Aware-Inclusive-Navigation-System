package com.group2.navigation.controller;

import com.group2.navigation.dto.SavedRouteRequest;
import com.group2.navigation.service.SavedRouteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saved-routes")
@Validated
@CrossOrigin(origins = "*")
public class SavedRouteController {

    @Autowired
    private SavedRouteService savedRouteService;

    /**
     * Save a new route.
     *
     * POST /api/saved-routes
     */
    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody SavedRouteRequest body) {
        try {
            Map<String, Object> result = savedRouteService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get all saved routes for a user.
     *
     * GET /api/saved-routes/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getByUser(
            @PathVariable @Min(value = 1, message = "userId must be a positive number") Long userId) {
        try {
            List<Map<String, Object>> routes = savedRouteService.findByUser(userId);
            return ResponseEntity.ok(Map.of("success", true, "routes", routes));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update a saved route.
     *
     * PUT /api/saved-routes/{routeId}
     */
    @PutMapping("/{routeId}")
    public ResponseEntity<Object> update(
            @PathVariable @Min(value = 1, message = "routeId must be a positive number") Long routeId,
            @Valid @RequestBody SavedRouteRequest body) {
        try {
            Map<String, Object> result = savedRouteService.update(routeId, body);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", e.getMessage()));
        }
    }

    /**
     * Delete a saved route.
     *
     * DELETE /api/saved-routes/{routeId}?userId={userId}
     */
    @DeleteMapping("/{routeId}")
    public ResponseEntity<Object> delete(
            @PathVariable @Min(value = 1, message = "routeId must be a positive number") Long routeId,
            @RequestParam @Min(value = 1, message = "userId must be a positive number") Long userId) {
        try {
            savedRouteService.delete(routeId, userId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Route deleted"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", e.getMessage()));
        }
    }
}
