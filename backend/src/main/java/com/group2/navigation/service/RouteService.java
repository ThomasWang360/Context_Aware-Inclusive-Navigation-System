package com.group2.navigation.service;

import com.group2.navigation.algorithm.AStarRouter;
import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.algorithm.Node;
import com.group2.navigation.model.RouteRequest;
import com.group2.navigation.model.RouteResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating routes.
 * 
 * TODO (Simon - lead, Thomas):
 * - Add caching for frequent routes
 * - Add logging
 */
@Service
public class RouteService {
    
    @Autowired
    private GraphService graphService;
    
    private AStarRouter router;
    
    @PostConstruct
    public void init() {
        // Initialize router with the loaded graph
        Graph graph = graphService.getGraph();
        this.router = new AStarRouter(graph);
    }
    
    /**
     * Calculate a route based on the request.
     */
    public RouteResponse calculateRoute(RouteRequest request) {
        // Find route using A*
        List<Node> path = router.findRoute(
            request.getStartLat(),
            request.getStartLng(),
            request.getEndLat(),
            request.getEndLng(),
            request.getPreferences()
        );
        
        if (path.isEmpty()) {
            return RouteResponse.error("No route found between the given points");
        }
        
        // Convert path to coordinate list for frontend
        List<double[]> coordinates = new ArrayList<>();
        for (Node node : path) {
            coordinates.add(new double[]{node.getLat(), node.getLng()});
        }
        
        // Calculate stats
        double distance = router.calculatePathDistance(path);
        double time = router.estimateWalkingTime(path);
        
        return new RouteResponse(coordinates, distance, time);
    }
}
