package com.group2.navigation.algorithm;

import java.util.HashMap;
import java.util.Map;

/**
 * Road network graph built from OpenStreetMap data.
 * 
 * TODO (Andrew - lead, Arshia, Simon, Maria):
 * - Implement OSM data parsing (use osm4j or similar library)
 * - Load graph from processed OSM file
 * - Add spatial indexing for efficient nearest-node lookup
 */
public class Graph {
    
    private Map<Long, Node> nodes;
    
    public Graph() {
        this.nodes = new HashMap<>();
    }
    
    /**
     * Add a node to the graph.
     */
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
    }
    
    /**
     * Get a node by its OSM ID.
     */
    public Node getNode(long id) {
        return nodes.get(id);
    }
    
    /**
     * Add an edge between two nodes.
     */
    public void addEdge(long sourceId, long targetId, double distance) {
        Node source = nodes.get(sourceId);
        Node target = nodes.get(targetId);
        
        if (source != null && target != null) {
            Edge edge = new Edge(source, target, distance);
            source.addEdge(edge);
            
            // For undirected roads, add reverse edge too
            Edge reverseEdge = new Edge(target, source, distance);
            target.addEdge(reverseEdge);
        }
    }
    
    /**
     * Find the nearest node to a given lat/lng coordinate.
     * 
     * TODO: This is a naive O(n) implementation. 
     * Replace with spatial index (R-tree or similar) for better performance.
     */
    public Node findNearestNode(double lat, double lng) {
        Node nearest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Node node : nodes.values()) {
            double dist = haversineDistance(lat, lng, node.getLat(), node.getLng());
            if (dist < minDistance) {
                minDistance = dist;
                nearest = node;
            }
        }
        
        return nearest;
    }
    
    /**
     * Calculate distance between two points using Haversine formula.
     * Returns distance in meters.
     */
    public static double haversineDistance(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371000; // Earth's radius in meters
        
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    /**
     * Reset all nodes for a new A* search.
     */
    public void resetNodes() {
        for (Node node : nodes.values()) {
            node.reset();
        }
    }
    
    /**
     * Get total number of nodes.
     */
    public int size() {
        return nodes.size();
    }
    
    /**
     * Get all nodes (for iteration).
     */
    public Iterable<Node> getAllNodes() {
        return nodes.values();
    }
    
    /**
     * Load graph from OSM data file.
     * 
     * TODO (Andrew - lead): Implement this method
     * - Parse OSM PBF file for GTA region
     * - Extract highway=* ways for walking/biking
     * - Create nodes and edges
     */
    public static Graph loadFromOSM(String filePath) {
        Graph graph = new Graph();
        
        // TODO: Implement OSM parsing
        // Suggested approach:
        // 1. Use osm4j library to read PBF file
        // 2. Filter for relevant highway types (footway, cycleway, path, etc.)
        // 3. Create Node objects for each OSM node
        // 4. Create Edge objects for each way segment
        
        System.out.println("TODO: Implement OSM loading from " + filePath);
        
        return graph;
    }
}
