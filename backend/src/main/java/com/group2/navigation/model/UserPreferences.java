package com.group2.navigation.model;

/**
 * User preferences for route calculation.
 * These determine how edge weights are calculated in the A* algorithm.
 */
public class UserPreferences {
    
    private boolean wheelchairAccessible;
    private boolean avoidHighCrime;
    private boolean preferLitStreets;
    private boolean avoidConstruction;
    private int timeOfDay; // 0-23, used for night mode calculations
    private double maxDistanceToHospital; // in meters, 0 = don't care
    
    // TODO: Add more preference fields as needed
    
    // Constructors
    public UserPreferences() {
        // Default preferences
        this.wheelchairAccessible = false;
        this.avoidHighCrime = false;
        this.preferLitStreets = false;
        this.avoidConstruction = false;
        this.timeOfDay = 12;
        this.maxDistanceToHospital = 0;
    }
    
    // Getters and Setters
    public boolean isWheelchairAccessible() {
        return wheelchairAccessible;
    }
    
    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }
    
    public boolean isAvoidHighCrime() {
        return avoidHighCrime;
    }
    
    public void setAvoidHighCrime(boolean avoidHighCrime) {
        this.avoidHighCrime = avoidHighCrime;
    }
    
    public boolean isPreferLitStreets() {
        return preferLitStreets;
    }
    
    public void setPreferLitStreets(boolean preferLitStreets) {
        this.preferLitStreets = preferLitStreets;
    }
    
    public boolean isAvoidConstruction() {
        return avoidConstruction;
    }
    
    public void setAvoidConstruction(boolean avoidConstruction) {
        this.avoidConstruction = avoidConstruction;
    }
    
    public int getTimeOfDay() {
        return timeOfDay;
    }
    
    public void setTimeOfDay(int timeOfDay) {
        this.timeOfDay = timeOfDay;
    }
    
    public double getMaxDistanceToHospital() {
        return maxDistanceToHospital;
    }
    
    public void setMaxDistanceToHospital(double maxDistanceToHospital) {
        this.maxDistanceToHospital = maxDistanceToHospital;
    }
}
