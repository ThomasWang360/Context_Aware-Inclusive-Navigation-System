package com.group2.navigation.algorithm;

import com.group2.navigation.model.UserPreferences;

import java.util.*;

/**
 * A* pathfinding algorithm implementation.
 * 
 * A* uses: f(n) = g(n) + h(n)
 * - g(n) = actual cost from start to node n (weighted by preferences)
 * - h(n) = heuristic estimate from n to goal (haversine distance)
 * 
 * TODO (Maria - lead, Andrew, Arshia, Simon):
 * - Fine-tune the heuristic for better performance
 * - Add support for multiple route alternatives
 * - Implement route caching for common queries
 */
public class AStarRouter {
    
    private Graph graph;
    
    public AStarRouter(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Find the optimal route from start to end based on user preferences.
     * 
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @param prefs User preferences for route weighting
     * @return List of nodes representing the path, or empty list if no path found
     */
    public List<Node> findRoute(double startLat, double startLng, 
                                 double endLat, double endLng,
                                 UserPreferences prefs) {
        
        // Reset graph state from previous searches
        graph.resetNodes();
        
        // Find nearest nodes to start and end coordinates
        Node start = graph.findNearestNode(startLat, startLng);
        Node goal = graph.findNearestNode(endLat, endLng);
        
        if (start == null || goal == null) {
            System.out.println("Could not find start or end node in graph");
            return Collections.emptyList();
        }
        
        // Priority queue ordered by f-score (lowest first)
        PriorityQueue<Node> openSet = new PriorityQueue<>(
            Comparator.comparingDouble(Node::getFScore)
        );
        
        Set<Node> closedSet = new HashSet<>();
        
        // Initialize start node
        start.setGScore(0);
        start.setFScore(heuristic(start, goal));
        openSet.add(start);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            // Found the goal!
            if (current.equals(goal)) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            // Explore neighbors
            for (Edge edge : current.getEdges()) {
                Node neighbor = edge.getTarget();
                
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                // Calculate tentative g-score through this path
                double tentativeGScore = current.getGScore() + edge.getWeightedCost(prefs);
                
                if (tentativeGScore < neighbor.getGScore()) {
                    // This is a better path
                    neighbor.setParent(current);
                    neighbor.setGScore(tentativeGScore);
                    neighbor.setFScore(tentativeGScore + heuristic(neighbor, goal));
                    
                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        
        // No path found
        System.out.println("No path found from start to goal");
        return Collections.emptyList();
    }
    
    /**
     * Heuristic function for A*.
     * Uses haversine distance (admissible since straight-line <= actual path).
     */
    private double heuristic(Node from, Node to) {
        return Graph.haversineDistance(
            from.getLat(), from.getLng(),
            to.getLat(), to.getLng()
        );
    }
    
    /**
     * Reconstruct the path from goal to start by following parent pointers.
     */
    private List<Node> reconstructPath(Node goal) {
        List<Node> path = new ArrayList<>();
        Node current = goal;
        
        while (current != null) {
            path.add(current);
            current = current.getParent();
        }
        
        Collections.reverse(path);
        return path;
    }
    
    /**
     * Calculate total distance of a path in meters.
     */
    public double calculatePathDistance(List<Node> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += Graph.haversineDistance(
                path.get(i).getLat(), path.get(i).getLng(),
                path.get(i + 1).getLat(), path.get(i + 1).getLng()
            );
        }
        return total;
    }
    
    /**
     * Estimate walking time based on path distance.
     * Assumes average walking speed of 5 km/h.
     */
    public double estimateWalkingTime(List<Node> path) {
        double distanceMeters = calculatePathDistance(path);
        double speedMps = 5000.0 / 3600.0; // 5 km/h in m/s
        return (distanceMeters / speedMps) / 60.0; // Return minutes
    }
}
