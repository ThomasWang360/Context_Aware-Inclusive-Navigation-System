package com.group2.navigation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SignupRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username may only contain letters, digits, and underscores")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Size(max = 50, message = "Display name must not exceed 50 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N} _.'-]*$", message = "Display name contains invalid characters")
    private String displayName;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Pattern(regexp = "^[\\p{L}\\p{N} ,.'-]*$", message = "Location contains invalid characters")
    private String location;

    public SignupRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
