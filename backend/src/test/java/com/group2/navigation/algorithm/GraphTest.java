package com.group2.navigation.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class GraphTest {

    private Graph graph;

    @BeforeEach
    void setUp() {
        graph = new Graph();
    }

    @Test
    void addNode_and_getNode() {
        Node node = new Node(42, 43.65, -79.38);
        graph.addNode(node);

        assertThat(graph.getNode(42)).isSameAs(node);
        assertThat(graph.size()).isEqualTo(1);
    }

    @Test
    void getNode_nonexistent_returnsNull() {
        assertThat(graph.getNode(999)).isNull();
    }

    @Test
    void addEdge_bidirectional() {
        graph.addNode(new Node(1, 43.65, -79.38));
        graph.addNode(new Node(2, 43.66, -79.37));
        graph.addEdge(1, 2, 100);

        assertThat(graph.getNode(1).getEdges()).hasSize(1);
        assertThat(graph.getNode(2).getEdges()).hasSize(1);
        assertThat(graph.getNode(1).getEdges().get(0).getTarget().getId()).isEqualTo(2);
        assertThat(graph.getNode(2).getEdges().get(0).getTarget().getId()).isEqualTo(1);
    }

    @Test
    void addEdge_withContextAttributes() {
        graph.addNode(new Node(1, 43.65, -79.38));
        graph.addNode(new Node(2, 43.66, -79.37));
        graph.addEdge(1, 2, 200, false, "gravel", false);

        Edge fwd = graph.getNode(1).getEdges().get(0);
        assertThat(fwd.isWheelchairAccessible()).isFalse();
        assertThat(fwd.getSurfaceType()).isEqualTo("gravel");
        assertThat(fwd.isLit()).isFalse();

        Edge rev = graph.getNode(2).getEdges().get(0);
        assertThat(rev.isWheelchairAccessible()).isFalse();
        assertThat(rev.getSurfaceType()).isEqualTo("gravel");
        assertThat(rev.isLit()).isFalse();
    }

    @Test
    void addEdge_nonexistentNode_noException() {
        graph.addNode(new Node(1, 43.65, -79.38));
        // Node 2 doesn't exist — should not throw
        graph.addEdge(1, 999, 100);
        assertThat(graph.getNode(1).getEdges()).isEmpty();
    }

    @Test
    void size_returnsNodeCount() {
        assertThat(graph.size()).isEqualTo(0);
        graph.addNode(new Node(1, 43.65, -79.38));
        assertThat(graph.size()).isEqualTo(1);
        graph.addNode(new Node(2, 43.66, -79.37));
        assertThat(graph.size()).isEqualTo(2);
    }

    @Test
    void getAllNodes_returnsAll() {
        graph.addNode(new Node(1, 43.65, -79.38));
        graph.addNode(new Node(2, 43.66, -79.37));

        List<Node> nodes = new ArrayList<>();
        graph.getAllNodes().forEach(nodes::add);
        assertThat(nodes).hasSize(2);
    }

    @Test
    void findNearestNode_emptyGraph_returnsNull() {
        assertThat(graph.findNearestNode(43.65, -79.38)).isNull();
    }

    @Test
    void findNearestNode_singleNode() {
        graph.addNode(new Node(1, 43.65, -79.38));
        Node nearest = graph.findNearestNode(43.651, -79.381);
        assertThat(nearest).isNotNull();
        assertThat(nearest.getId()).isEqualTo(1);
    }

    @Test
    void findNearestNode_pickClosest() {
        graph.addNode(new Node(1, 43.6500, -79.3800));
        graph.addNode(new Node(2, 43.6600, -79.3700));
        graph.addNode(new Node(3, 43.6700, -79.3600));

        Node nearest = graph.findNearestNode(43.6505, -79.3805);
        assertThat(nearest.getId()).isEqualTo(1);
    }

    @Test
    void findNearestNode_farFromAllNodes() {
        graph.addNode(new Node(1, 43.65, -79.38));
        graph.addNode(new Node(2, 43.75, -79.28));

        Node nearest = graph.findNearestNode(43.70, -79.33);
        assertThat(nearest).isNotNull();
    }

    @Test
    void haversineDistance_samePoint_returnsZero() {
        assertThat(Graph.haversineDistance(43.65, -79.38, 43.65, -79.38)).isEqualTo(0);
    }

    @Test
    void haversineDistance_knownDistance() {
        // CN Tower (43.6426, -79.3871) to Union Station (43.6453, -79.3806)
        // Approx 570m
        double dist = Graph.haversineDistance(43.6426, -79.3871, 43.6453, -79.3806);
        assertThat(dist).isBetween(500.0, 700.0);
    }

    @Test
    void haversineDistance_symmetric() {
        double d1 = Graph.haversineDistance(43.65, -79.38, 43.66, -79.37);
        double d2 = Graph.haversineDistance(43.66, -79.37, 43.65, -79.38);
        assertThat(d1).isEqualTo(d2, within(0.001));
    }

    @Test
    void haversineDistance_largerDistance() {
        // Toronto to Mississauga ~ 25km
        double dist = Graph.haversineDistance(43.6532, -79.3832, 43.5890, -79.6441);
        assertThat(dist).isBetween(20000.0, 30000.0);
    }

    @Test
    void resetNodes_clearsAStarState() {
        graph.addNode(new Node(1, 43.65, -79.38));
        graph.addNode(new Node(2, 43.66, -79.37));

        graph.getNode(1).setGScore(5.0);
        graph.getNode(1).setFScore(10.0);
        graph.getNode(2).setGScore(3.0);

        graph.resetNodes();

        assertThat(graph.getNode(1).getGScore()).isEqualTo(Double.MAX_VALUE);
        assertThat(graph.getNode(1).getFScore()).isEqualTo(Double.MAX_VALUE);
        assertThat(graph.getNode(1).getParent()).isNull();
        assertThat(graph.getNode(2).getGScore()).isEqualTo(Double.MAX_VALUE);
    }

    @Test
    void retainLargestComponent_keepsConnected() {
        // Component 1: nodes 1-2-3 (connected)
        graph.addNode(new Node(1, 43.650, -79.380));
        graph.addNode(new Node(2, 43.651, -79.379));
        graph.addNode(new Node(3, 43.652, -79.378));
        graph.addEdge(1, 2, 100);
        graph.addEdge(2, 3, 100);

        // Component 2: node 4 (isolated)
        graph.addNode(new Node(4, 43.700, -79.300));

        assertThat(graph.size()).isEqualTo(4);

        graph.retainLargestComponent();

        assertThat(graph.size()).isEqualTo(3);
        assertThat(graph.getNode(1)).isNotNull();
        assertThat(graph.getNode(2)).isNotNull();
        assertThat(graph.getNode(3)).isNotNull();
        assertThat(graph.getNode(4)).isNull();
    }

    @Test
    void retainLargestComponent_emptyGraph() {
        graph.retainLargestComponent();
        assertThat(graph.size()).isEqualTo(0);
    }

    @Test
    void retainLargestComponent_singleNode() {
        graph.addNode(new Node(1, 43.65, -79.38));
        graph.retainLargestComponent();
        assertThat(graph.size()).isEqualTo(1);
    }

    @Test
    void retainLargestComponent_twoComponents_keepsLarger() {
        // Component A: 3 nodes
        graph.addNode(new Node(1, 43.650, -79.380));
        graph.addNode(new Node(2, 43.651, -79.379));
        graph.addNode(new Node(3, 43.652, -79.378));
        graph.addEdge(1, 2, 100);
        graph.addEdge(2, 3, 100);

        // Component B: 2 nodes
        graph.addNode(new Node(10, 43.700, -79.300));
        graph.addNode(new Node(11, 43.701, -79.299));
        graph.addEdge(10, 11, 100);

        graph.retainLargestComponent();

        assertThat(graph.size()).isEqualTo(3);
        assertThat(graph.getNode(10)).isNull();
        assertThat(graph.getNode(11)).isNull();
    }

    @Test
    void retainLargestComponent_spatialIndexRebuilt() {
        graph.addNode(new Node(1, 43.650, -79.380));
        graph.addNode(new Node(2, 43.651, -79.379));
        graph.addEdge(1, 2, 100);
        graph.addNode(new Node(99, 43.800, -79.200)); // isolated

        graph.retainLargestComponent();

        // Spatial index should still work for remaining nodes
        Node nearest = graph.findNearestNode(43.650, -79.380);
        assertThat(nearest).isNotNull();
        assertThat(nearest.getId()).isEqualTo(1);

        // After pruning, the isolated node is gone.
        // The spatial index only covers the remaining component's cells,
        // so a query far from them may return null if out of search radius.
        Node farNearest = graph.findNearestNode(43.800, -79.200);
        if (farNearest != null) {
            assertThat(farNearest.getId()).isNotEqualTo(99);
        }
    }
}
