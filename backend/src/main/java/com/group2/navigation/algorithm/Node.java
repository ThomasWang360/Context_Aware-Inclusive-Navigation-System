package com.group2.navigation.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node (intersection/point) in the road network graph.
 * 
 * TODO (Andrew, Arshia, Simon, Maria): 
 * - Add more attributes from OSM data (e.g., traffic signals, crosswalks)
 * - Consider adding elevation data for wheelchair routing
 */
public class Node {
    
    private long id; // OSM node ID
    private double lat;
    private double lng;
    private List<Edge> edges;
    
    // For A* algorithm
    private double gScore; // Cost from start to this node
    private double fScore; // gScore + heuristic estimate to goal
    private Node parent; // For path reconstruction
    
    public Node(long id, double lat, double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
        this.edges = new ArrayList<>();
        this.gScore = Double.MAX_VALUE;
        this.fScore = Double.MAX_VALUE;
        this.parent = null;
    }
    
    public void addEdge(Edge edge) {
        this.edges.add(edge);
    }
    
    /**
     * Reset A* scores for a new search.
     */
    public void reset() {
        this.gScore = Double.MAX_VALUE;
        this.fScore = Double.MAX_VALUE;
        this.parent = null;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public double getLat() {
        return lat;
    }
    
    public double getLng() {
        return lng;
    }
    
    public List<Edge> getEdges() {
        return edges;
    }
    
    public double getGScore() {
        return gScore;
    }
    
    public void setGScore(double gScore) {
        this.gScore = gScore;
    }
    
    public double getFScore() {
        return fScore;
    }
    
    public void setFScore(double fScore) {
        this.fScore = fScore;
    }
    
    public Node getParent() {
        return parent;
    }
    
    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Node node = (Node) obj;
        return id == node.id;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
