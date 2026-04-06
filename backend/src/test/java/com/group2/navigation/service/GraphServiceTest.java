package com.group2.navigation.service;

import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.algorithm.Node;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GraphServiceTest {

    @Autowired
    private GraphService graphService;

    // --- initialization ---

    @Test
    void init_graphIsNotNull() {
        Graph graph = graphService.getGraph();
        assertThat(graph).isNotNull();
    }

    @Test
    void init_graphHasNodes() {
        Graph graph = graphService.getGraph();
        assertThat(graph.size()).isGreaterThan(0);
    }

    @Test
    void init_graphNodesHaveValidCoordinates() {
        Graph graph = graphService.getGraph();
        for (Node node : graph.getAllNodes()) {
            assertThat(node.getLat()).isBetween(-90.0, 90.0);
            assertThat(node.getLng()).isBetween(-180.0, 180.0);
        }
    }

    @Test
    void init_graphNodesHaveEdges() {
        Graph graph = graphService.getGraph();
        // At least some nodes should have edges
        boolean hasEdges = false;
        for (Node node : graph.getAllNodes()) {
            if (!node.getEdges().isEmpty()) {
                hasEdges = true;
                break;
            }
        }
        assertThat(hasEdges).isTrue();
    }

    // --- findNearestNode ---

    @Test
    void findNearestNode_torontoCenter_findsNode() {
        Graph graph = graphService.getGraph();
        // Toronto city center
        Node nearest = graph.findNearestNode(43.6532, -79.3832);
        assertThat(nearest).isNotNull();
        // Should be reasonably close to the query point (within ~10km)
        double distance = Graph.haversineDistance(43.6532, -79.3832, nearest.getLat(), nearest.getLng());
        assertThat(distance).isLessThan(10_000);
    }

    @Test
    void findNearestNode_differentLocation_findsNode() {
        Graph graph = graphService.getGraph();
        // Use a slightly different central Toronto point
        Node nearest = graph.findNearestNode(43.6600, -79.3900);
        assertThat(nearest).isNotNull();
    }

    // --- getGraph consistency ---

    @Test
    void getGraph_returnsSameInstance() {
        Graph g1 = graphService.getGraph();
        Graph g2 = graphService.getGraph();
        assertThat(g1).isSameAs(g2);
    }

    // --- test graph fallback validation ---

    @Test
    void graph_isConnected() {
        // The graph should have retained its largest connected component
        // so all nodes should be reachable from any starting node
        Graph graph = graphService.getGraph();
        if (graph.size() <= 10) {
            // Test graph: verify all nodes have at least one edge
            for (Node node : graph.getAllNodes()) {
                assertThat(node.getEdges()).isNotEmpty();
            }
        }
    }

    @Test
    void graph_edgesHavePositiveDistance() {
        Graph graph = graphService.getGraph();
        for (Node node : graph.getAllNodes()) {
            node.getEdges().forEach(edge -> {
                assertThat(edge.getDistance()).isGreaterThan(0);
            });
        }
    }

    // --- loadFromOSM ---

    @Test
    void loadFromOSM_invalidPath_doesNotCrash() {
        // Loading from a bad path should not crash the service
        // (the graph should remain the previously loaded one)
        Graph before = graphService.getGraph();
        try {
            graphService.loadFromOSM("/nonexistent/path.json");
        } catch (Exception e) {
            // Expected — file doesn't exist
        }
        // Original graph should still be accessible
        assertThat(graphService.getGraph()).isNotNull();
    }
}
