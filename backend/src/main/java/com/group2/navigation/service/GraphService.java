package com.group2.navigation.service;

import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.algorithm.Node;
import com.group2.navigation.data.OsmDataLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Manages the road network graph.
 *
 * On startup, attempts to load real OpenStreetMap data for Toronto via the
 * Overpass API (cached locally after first download). Falls back to a small
 * hardcoded test graph if the download fails.
 */
@Service
public class GraphService {

    private Graph graph;

    @PostConstruct
    public void init() {
        try {
            String osmPath = OsmDataLoader.ensureOsmData();
            if (osmPath != null) {
                Graph osmGraph = Graph.loadFromOSM(osmPath);
                if (osmGraph.size() > 0) {
                    osmGraph.retainLargestComponent();
                    this.graph = osmGraph;
                    System.out.println("[GraphService] Real OSM graph ready: "
                            + graph.size() + " nodes");
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("[GraphService] OSM load failed: " + e.getMessage());
        }

        System.out.println("[GraphService] Falling back to test graph");
        this.graph = createTestGraph();
        System.out.println("[GraphService] Test graph loaded: " + graph.size() + " nodes");
    }

    public Graph getGraph() {
        return graph;
    }

    /**
     * Small hardcoded graph for development/testing when OSM data is unavailable.
     */
    private Graph createTestGraph() {
        Graph g = new Graph();

        g.addNode(new Node(1, 43.6532, -79.3832)); // CN Tower area
        g.addNode(new Node(2, 43.6544, -79.3807)); // Union Station
        g.addNode(new Node(3, 43.6565, -79.3800)); // King St
        g.addNode(new Node(4, 43.6576, -79.3851)); // Entertainment District
        g.addNode(new Node(5, 43.6510, -79.3470)); // Distillery District

        g.addEdge(1, 2, 250);
        g.addEdge(2, 3, 230);
        g.addEdge(3, 4, 450);
        g.addEdge(1, 4, 380);
        g.addEdge(2, 5, 2800);
        g.addEdge(3, 5, 2650);

        return g;
    }

    public void loadFromOSM(String filePath) {
        this.graph = Graph.loadFromOSM(filePath);
    }
}
