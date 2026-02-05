package com.group2.navigation.model;

/**
 * Request body for route calculation endpoint.
 */
public class RouteRequest {
    
    private double startLat;
    private double startLng;
    private double endLat;
    private double endLng;
    private UserPreferences preferences;
    
    // Constructors
    public RouteRequest() {}
    
    public RouteRequest(double startLat, double startLng, double endLat, double endLng) {
        this.startLat = startLat;
        this.startLng = startLng;
        this.endLat = endLat;
        this.endLng = endLng;
        this.preferences = new UserPreferences();
    }
    
    // Getters and Setters
    public double getStartLat() {
        return startLat;
    }
    
    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }
    
    public double getStartLng() {
        return startLng;
    }
    
    public void setStartLng(double startLng) {
        this.startLng = startLng;
    }
    
    public double getEndLat() {
        return endLat;
    }
    
    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }
    
    public double getEndLng() {
        return endLng;
    }
    
    public void setEndLng(double endLng) {
        this.endLng = endLng;
    }
    
    public UserPreferences getPreferences() {
        return preferences;
    }
    
    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }
}
