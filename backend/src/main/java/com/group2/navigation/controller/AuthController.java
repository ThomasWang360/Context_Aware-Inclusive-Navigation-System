package com.group2.navigation.controller;

import com.group2.navigation.dto.LoginRequest;
import com.group2.navigation.dto.SignupRequest;
import com.group2.navigation.dto.UpdateUserCredentialsRequest;
import com.group2.navigation.model.User;
import com.group2.navigation.model.UserPreferences;
import com.group2.navigation.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST endpoints for user signup, login, and preference management.
 *
 * Stateless — the frontend stores the userId after login and sends it
 * with preference-related requests. No session or JWT needed for the prototype.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
@Validated
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Register a new user.
     *
     * POST /api/auth/signup
     * Body: { "username": "arshia", "password": "pass1234", "displayName": "Arshia" }
     */
    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody SignupRequest body) {
        try {
            String username = body.getUsername().trim();
            String password = body.getPassword();
            String displayName = sanitizeDisplayName(body.getDisplayName());
            String location = normalizeLocation(body.getLocation());

            User user = authService.signup(username, password, displayName, location);
            return ResponseEntity.ok(userResponse(user, "Account created"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Log in with existing credentials.
     *
     * POST /api/auth/login
     * Body: { "username": "arshia", "password": "pass1234" }
     */
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest body) {
        try {
            String username = body.getUsername().trim();
            String password = body.getPassword();

            User user = authService.login(username, password);
            return ResponseEntity.ok(userResponse(user, "Login successful"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Get a user's profile and saved preferences.
     *
     * GET /api/auth/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Object> getUser(
            @PathVariable @Min(value = 1, message = "userId must be a positive number") Long userId) {
        return authService.getUser(userId)
                .map(user -> ResponseEntity.ok((Object) userResponse(user, "User found")))
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found")));
    }

    /**
     * Get a user's saved preferences.
     *
     * GET /api/auth/preferences/{userId}
     */
    @GetMapping("/preferences/{userId}")
    public ResponseEntity<Object> getPreferences(
            @PathVariable @Min(value = 1, message = "userId must be a positive number") Long userId) {
        return authService.getUser(userId)
                .map(user -> ResponseEntity.ok((Object) Map.of(
                        "success", true,
                        "userId", user.getId(),
                        "preferences", user.toPreferences())))
                .orElse(ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found")));
    }

    /**
     * Update a user's saved preferences.
     *
     * PUT /api/auth/preferences/{userId}
     * Body: { "wheelchairWeight": 10, "crimeWeight": 5, ... }
     */
    @PutMapping("/preferences/{userId}")
    public ResponseEntity<Object> updatePreferences(
            @PathVariable @Min(value = 1, message = "userId must be a positive number") Long userId,
            @Valid @RequestBody UserPreferences prefs) {
        try {
            User user = authService.updatePreferences(userId, prefs);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Preferences updated",
                    "preferences", user.toPreferences()));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Update login email and/or password.
     *
     * PUT /api/auth/user/{id}
     */
    @PutMapping("/user/{id}")
    public ResponseEntity<Object> updateUserCredentials(
            @PathVariable @Min(value = 1, message = "id must be a positive number") Long id,
            @Valid @RequestBody UpdateUserCredentialsRequest body) {
        try {
            User user = authService.updateCredentials(id, body.getEmail(), body.getPassword());
            return ResponseEntity.ok(userResponse(user, "User updated"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * Delete account and related messages.
     *
     * DELETE /api/auth/user/{id}
     */
    @DeleteMapping("/user/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Min(value = 1, message = "id must be a positive number") Long id) {
        try {
            authService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /** Build a consistent user response (never expose the password hash). */
    private Map<String, Object> userResponse(User user, String message) {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", message);
        resp.put("userId", user.getId());
        resp.put("username", user.getUsername());
        resp.put("displayName", user.getDisplayName());
        resp.put("email", user.getEmail());
        resp.put("location", user.getLocation());
        resp.put("preferences", user.toPreferences());
        return resp;
    }

    private String normalizeLocation(String location) {
        if (location == null) {
            return null;
        }
        String t = location.trim();
        return t.isEmpty() ? null : t;
    }

    /** Strip HTML tags from display name to prevent stored XSS. */
    private String sanitizeDisplayName(String name) {
        if (name == null) return null;
        return name.replaceAll("<[^>]*>", "").trim();
    }
}
