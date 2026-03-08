package com.group2.navigation.model;

/**
 * User preferences for route calculation.
 *
 * Each weight is 0-10 where 0 = "don't care" and 10 = "maximum importance".
 * The A* edge-cost formula scales penalties proportionally to these weights.
 */
public class UserPreferences {

    private double wheelchairWeight;     // 0-10, avoid non-accessible paths
    private double crimeWeight;          // 0-10, avoid high-crime areas
    private double lightingWeight;       // 0-10, prefer lit streets
    private double constructionWeight;   // 0-10, avoid construction zones
    private int timeOfDay;               // 0-23, affects lighting penalty strength
    private double maxDistanceToHospital; // meters, 0 = don't care

    public UserPreferences() {
        this.wheelchairWeight = 0;
        this.crimeWeight = 0;
        this.lightingWeight = 0;
        this.constructionWeight = 0;
        this.timeOfDay = 12;
        this.maxDistanceToHospital = 0;
    }

    public double getWheelchairWeight() {
        return wheelchairWeight;
    }

    public void setWheelchairWeight(double wheelchairWeight) {
        this.wheelchairWeight = wheelchairWeight;
    }

    public double getCrimeWeight() {
        return crimeWeight;
    }

    public void setCrimeWeight(double crimeWeight) {
        this.crimeWeight = crimeWeight;
    }

    public double getLightingWeight() {
        return lightingWeight;
    }

    public void setLightingWeight(double lightingWeight) {
        this.lightingWeight = lightingWeight;
    }

    public double getConstructionWeight() {
        return constructionWeight;
    }

    public void setConstructionWeight(double constructionWeight) {
        this.constructionWeight = constructionWeight;
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
