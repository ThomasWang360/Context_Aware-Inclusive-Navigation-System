package com.group2.navigation.algorithm;

/**
 * Represents an edge (road segment) in the road network graph.
 * 
 * TODO (Andrew, Arshia, Simon, Maria):
 * - Add more OSM attributes (surface type, highway type, etc.)
 * - Implement weight calculation based on context data
 */
public class Edge {
    
    private Node source;
    private Node target;
    private double distance; // Base distance in meters
    
    // Context attributes (from OSM and city data)
    private boolean wheelchairAccessible;
    private boolean lit; // Has street lighting
    private double crimeScore; // 0-1, higher = more crime
    private boolean hasConstruction;
    private String surfaceType; // paved, gravel, etc.
    
    public Edge(Node source, Node target, double distance) {
        this.source = source;
        this.target = target;
        this.distance = distance;
        
        // Default values
        this.wheelchairAccessible = true;
        this.lit = true;
        this.crimeScore = 0;
        this.hasConstruction = false;
        this.surfaceType = "paved";
    }
    
    /**
     * Calculate the weighted cost of this edge based on user preferences.
     * 
     * TODO (Maria - lead, Andrew, Arshia, Simon):
     * Implement the weight formula:
     * weight = baseDistance 
     *        + (crimeScore * crimeWeight)
     *        + (lightingPenalty * lightWeight)
     *        + (accessibilityPenalty * accessWeight)
     *        + (constructionPenalty * constructionWeight)
     */
    public double getWeightedCost(com.group2.navigation.model.UserPreferences prefs) {
        double weight = distance;
        
        // TODO: Implement weighted cost calculation
        // This is where the magic happens - adjust weights based on preferences
        
        // Example (to be refined):
        // if (prefs.isAvoidHighCrime()) {
        //     weight += crimeScore * 1000; // Heavy penalty for crime
        // }
        // if (prefs.isPreferLitStreets() && !lit) {
        //     weight += 500; // Penalty for unlit streets
        // }
        // if (prefs.isWheelchairAccessible() && !wheelchairAccessible) {
        //     weight = Double.MAX_VALUE; // Can't use this edge
        // }
        
        return weight;
    }
    
    // Getters and Setters
    public Node getSource() {
        return source;
    }
    
    public Node getTarget() {
        return target;
    }
    
    public double getDistance() {
        return distance;
    }
    
    public boolean isWheelchairAccessible() {
        return wheelchairAccessible;
    }
    
    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }
    
    public boolean isLit() {
        return lit;
    }
    
    public void setLit(boolean lit) {
        this.lit = lit;
    }
    
    public double getCrimeScore() {
        return crimeScore;
    }
    
    public void setCrimeScore(double crimeScore) {
        this.crimeScore = crimeScore;
    }
    
    public boolean hasConstruction() {
        return hasConstruction;
    }
    
    public void setHasConstruction(boolean hasConstruction) {
        this.hasConstruction = hasConstruction;
    }
    
    public String getSurfaceType() {
        return surfaceType;
    }
    
    public void setSurfaceType(String surfaceType) {
        this.surfaceType = surfaceType;
    }
}
