package com.group2.navigation.algorithm;

import com.group2.navigation.model.UserPreferences;
import com.group2.navigation.repository.ContextDataRepository;

/**
 * Represents an edge (road segment) in the road network graph.
 *
 * Cost formula (all penalties scale linearly with the user's 0-10 weight):
 *   cost = baseDistance
 *        + wheelchair penalty  (weight * 500, so max 5000 m-equivalent at 10)
 *        + crime penalty       (crimeScore * weight * 100, so max 1000 at 10)
 *        + lighting penalty    (weight * 50 night / weight * 5 day, so max 500/50)
 *        + construction penalty(weight * 80, so max 800)
 */
public class Edge {

    private Node source;
    private Node target;
    private double distance;

    private boolean wheelchairAccessible;
    private boolean lit;
    private double crimeScore;     // 0-1
    private boolean hasConstruction;
    private String surfaceType;

    public Edge(Node source, Node target, double distance) {
        this.source = source;
        this.target = target;
        this.distance = distance;
        this.wheelchairAccessible = true;
        this.lit = true;
        this.crimeScore = 0;
        this.hasConstruction = false;
        this.surfaceType = "paved";
    }

    /**
     * Weighted cost using pre-loaded context data on the edge.
     */
    public double getWeightedCost(UserPreferences prefs) {
        double cost = distance;

        if (prefs.getWheelchairWeight() > 0 && !wheelchairAccessible) {
            cost += prefs.getWheelchairWeight() * 500.0;
        }

        if (prefs.getCrimeWeight() > 0) {
            cost += crimeScore * prefs.getCrimeWeight() * 100.0;
        }

        if (prefs.getLightingWeight() > 0 && !lit) {
            int hour = prefs.getTimeOfDay();
            boolean night = hour >= 20 || hour < 6;
            cost += prefs.getLightingWeight() * (night ? 50.0 : 5.0);
        }

        if (prefs.getConstructionWeight() > 0 && hasConstruction) {
            cost += prefs.getConstructionWeight() * 80.0;
        }

        return cost;
    }

    /**
     * Weighted cost with live database queries for context data.
     */
    public double getWeightedCost(UserPreferences prefs, ContextDataRepository contextRepo) {
        double cost = distance;

        if (prefs.getWheelchairWeight() > 0 && !wheelchairAccessible) {
            cost += prefs.getWheelchairWeight() * 500.0;
        }

        double midLat = (source.getLat() + target.getLat()) / 2.0;
        double midLng = (source.getLng() + target.getLng()) / 2.0;

        if (prefs.getCrimeWeight() > 0) {
            double liveCrime = contextRepo.getCrimeScore(midLat, midLng);
            cost += liveCrime * prefs.getCrimeWeight() * 100.0;
        }

        if (prefs.getLightingWeight() > 0) {
            boolean hasLight = contextRepo.hasStreetLighting(midLat, midLng);
            if (!hasLight) {
                int hour = prefs.getTimeOfDay();
                boolean night = hour >= 20 || hour < 6;
                cost += prefs.getLightingWeight() * (night ? 50.0 : 5.0);
            }
        }

        if (prefs.getConstructionWeight() > 0) {
            boolean nearConstruction = contextRepo.hasConstruction(midLat, midLng);
            if (nearConstruction) {
                cost += prefs.getConstructionWeight() * 80.0;
            }
        }

        if (prefs.getMaxDistanceToHospital() > 0) {
            double hospitalDist = contextRepo.getDistanceToNearestHospital(midLat, midLng);
            if (hospitalDist > prefs.getMaxDistanceToHospital()) {
                double excess = hospitalDist - prefs.getMaxDistanceToHospital();
                cost += excess * 0.5;
            }
        }

        return cost;
    }

    // Getters and Setters
    public Node getSource() { return source; }
    public Node getTarget() { return target; }
    public double getDistance() { return distance; }

    public boolean isWheelchairAccessible() { return wheelchairAccessible; }
    public void setWheelchairAccessible(boolean v) { this.wheelchairAccessible = v; }

    public boolean isLit() { return lit; }
    public void setLit(boolean v) { this.lit = v; }

    public double getCrimeScore() { return crimeScore; }
    public void setCrimeScore(double v) { this.crimeScore = v; }

    public boolean hasConstruction() { return hasConstruction; }
    public void setHasConstruction(boolean v) { this.hasConstruction = v; }

    public String getSurfaceType() { return surfaceType; }
    public void setSurfaceType(String v) { this.surfaceType = v; }
}
