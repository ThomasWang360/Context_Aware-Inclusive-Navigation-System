package com.group2.navigation.service;

import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.algorithm.Node;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service for managing the road network graph.
 * 
 * TODO (Andrew, Arshia, Simon, Maria):
 * - Load real graph from OSM data
 * - Add context data overlay (crime, lighting, etc.)
 * - Implement graph updates for construction data
 */
@Service
public class GraphService {
    
    private Graph graph;
    
    @PostConstruct
    public void init() {
        // TODO: Replace with actual OSM data loading
        // For now, create a small test graph
        this.graph = createTestGraph();
        
        System.out.println("Graph loaded with " + graph.size() + " nodes");
    }
    
    public Graph getGraph() {
        return graph;
    }
    
    /**
     * Create a small test graph for development.
     * This should be replaced with real OSM data.
     * 
     * Test area: Downtown Toronto
     */
    private Graph createTestGraph() {
        Graph g = new Graph();
        
        // Sample nodes around downtown Toronto
        // Node format: (id, lat, lng)
        g.addNode(new Node(1, 43.6532, -79.3832)); // CN Tower area
        g.addNode(new Node(2, 43.6544, -79.3807)); // Union Station
        g.addNode(new Node(3, 43.6565, -79.3800)); // King St
        g.addNode(new Node(4, 43.6576, -79.3851)); // Entertainment District
        g.addNode(new Node(5, 43.6510, -79.3470)); // Distillery District
        
        // Add some edges (connections between nodes)
        // Distance is approximate, in meters
        g.addEdge(1, 2, 250);
        g.addEdge(2, 3, 230);
        g.addEdge(3, 4, 450);
        g.addEdge(1, 4, 380);
        g.addEdge(2, 5, 2800);
        g.addEdge(3, 5, 2650);
        
        return g;
    }
    
    /**
     * Load the full GTA graph from OSM data.
     * 
     * TODO: Implement this method
     */
    public void loadFromOSM(String filePath) {
        this.graph = Graph.loadFromOSM(filePath);
    }
}
