package com.group2.navigation.model;

import java.util.List;

/**
 * Response containing the calculated route.
 */
public class RouteResponse {
    
    private List<double[]> coordinates; // List of [lat, lng] pairs
    private double totalDistance; // in meters
    private double estimatedTime; // in minutes
    private String message;
    private boolean success;
    
    // Constructors
    public RouteResponse() {
        this.success = false;
    }
    
    public RouteResponse(List<double[]> coordinates, double totalDistance, double estimatedTime) {
        this.coordinates = coordinates;
        this.totalDistance = totalDistance;
        this.estimatedTime = estimatedTime;
        this.success = true;
        this.message = "Route calculated successfully";
    }
    
    public static RouteResponse error(String message) {
        RouteResponse response = new RouteResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    // Getters and Setters
    public List<double[]> getCoordinates() {
        return coordinates;
    }
    
    public void setCoordinates(List<double[]> coordinates) {
        this.coordinates = coordinates;
    }
    
    public double getTotalDistance() {
        return totalDistance;
    }
    
    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }
    
    public double getEstimatedTime() {
        return estimatedTime;
    }
    
    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
