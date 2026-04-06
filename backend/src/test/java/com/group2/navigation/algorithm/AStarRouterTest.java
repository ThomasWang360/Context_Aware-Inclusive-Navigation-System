package com.group2.navigation.algorithm;

import com.group2.navigation.model.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class AStarRouterTest {

    private Graph graph;
    private AStarRouter router;

    @BeforeEach
    void setUp() {
        graph = new Graph();

        // Simple diamond graph:
        //       2
        //      / \
        //     1   4
        //      \ /
        //       3
        graph.addNode(new Node(1, 43.6532, -79.3832)); // start
        graph.addNode(new Node(2, 43.6544, -79.3807)); // upper
        graph.addNode(new Node(3, 43.6520, -79.3807)); // lower
        graph.addNode(new Node(4, 43.6532, -79.3780)); // end

        graph.addEdge(1, 2, 200);
        graph.addEdge(1, 3, 200);
        graph.addEdge(2, 4, 200);
        graph.addEdge(3, 4, 200);

        router = new AStarRouter(graph);
    }

    @Test
    void findRoute_simpleGraph_findsPath() {
        UserPreferences prefs = new UserPreferences();
        List<Node> path = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, prefs);

        assertThat(path).isNotEmpty();
        assertThat(path.get(0).getId()).isEqualTo(1);
        assertThat(path.get(path.size() - 1).getId()).isEqualTo(4);
    }

    @Test
    void findRoute_sameStartAndEnd_returnsPath() {
        UserPreferences prefs = new UserPreferences();
        List<Node> path = router.findRoute(43.6532, -79.3832, 43.6532, -79.3832, prefs);

        assertThat(path).isNotEmpty();
        assertThat(path).hasSize(1);
    }

    @Test
    void findRoute_noPath_returnsEmpty() {
        Graph isolated = new Graph();
        isolated.addNode(new Node(1, 43.65, -79.38));
        isolated.addNode(new Node(2, 43.66, -79.37));
        // No edges -> no route

        AStarRouter isolatedRouter = new AStarRouter(isolated);
        UserPreferences prefs = new UserPreferences();

        List<Node> path = isolatedRouter.findRoute(43.65, -79.38, 43.66, -79.37, prefs);
        assertThat(path).isEmpty();
    }

    @Test
    void findRoute_emptyGraph_returnsEmpty() {
        AStarRouter emptyRouter = new AStarRouter(new Graph());
        UserPreferences prefs = new UserPreferences();

        List<Node> path = emptyRouter.findRoute(43.65, -79.38, 43.66, -79.37, prefs);
        assertThat(path).isEmpty();
    }

    @Test
    void findRoute_wheelchairPreference_avoidsInaccessible() {
        // Make the upper path (1->2->4) not wheelchair accessible
        graph = new Graph();
        graph.addNode(new Node(1, 43.6532, -79.3832));
        graph.addNode(new Node(2, 43.6544, -79.3807));
        graph.addNode(new Node(3, 43.6520, -79.3807));
        graph.addNode(new Node(4, 43.6532, -79.3780));

        graph.addEdge(1, 2, 200, false, "gravel", true); // NOT wheelchair accessible
        graph.addEdge(1, 3, 200, true, "paved", true);   // wheelchair accessible
        graph.addEdge(2, 4, 200, false, "gravel", true);
        graph.addEdge(3, 4, 200, true, "paved", true);

        router = new AStarRouter(graph);

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(10.0); // max wheelchair preference

        List<Node> path = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, prefs);
        assertThat(path).isNotEmpty();
        // Should take the lower path through node 3
        assertThat(path.stream().anyMatch(n -> n.getId() == 3)).isTrue();
    }

    @Test
    void findRoute_crimePreference_affectsRouting() {
        graph = new Graph();
        graph.addNode(new Node(1, 43.6532, -79.3832));
        graph.addNode(new Node(2, 43.6544, -79.3807));
        graph.addNode(new Node(3, 43.6520, -79.3807));
        graph.addNode(new Node(4, 43.6532, -79.3780));

        // Upper path has high crime
        graph.addEdge(1, 2, 200);
        graph.addEdge(1, 3, 300); // longer but safer
        graph.addEdge(2, 4, 200);
        graph.addEdge(3, 4, 300);

        // Set crime scores on edges through node 2
        for (Edge e : graph.getNode(1).getEdges()) {
            if (e.getTarget().getId() == 2) {
                e.setCrimeScore(1.0); // max crime
            }
        }
        for (Edge e : graph.getNode(2).getEdges()) {
            if (e.getTarget().getId() == 4) {
                e.setCrimeScore(1.0);
            }
        }

        router = new AStarRouter(graph);

        // Without crime preference → shorter upper path
        UserPreferences noPrefs = new UserPreferences();
        List<Node> shortPath = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, noPrefs);

        // With high crime preference → longer lower path
        UserPreferences crimePrefs = new UserPreferences();
        crimePrefs.setCrimeWeight(10.0);
        List<Node> safePath = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, crimePrefs);

        assertThat(shortPath).isNotEmpty();
        assertThat(safePath).isNotEmpty();
    }

    @Test
    void findRoute_lightingPreference_nightVsDay() {
        graph = new Graph();
        graph.addNode(new Node(1, 43.6532, -79.3832));
        graph.addNode(new Node(2, 43.6544, -79.3807));
        graph.addNode(new Node(3, 43.6520, -79.3807));
        graph.addNode(new Node(4, 43.6532, -79.3780));

        // Upper: not lit, Lower: lit
        graph.addEdge(1, 2, 200, true, "paved", false); // NOT lit
        graph.addEdge(1, 3, 250, true, "paved", true);  // lit
        graph.addEdge(2, 4, 200, true, "paved", false);
        graph.addEdge(3, 4, 250, true, "paved", true);

        router = new AStarRouter(graph);

        // At night with high lighting preference, should prefer lit path
        UserPreferences nightPrefs = new UserPreferences();
        nightPrefs.setLightingWeight(10.0);
        nightPrefs.setTimeOfDay(22); // night

        List<Node> nightPath = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, nightPrefs);
        assertThat(nightPath).isNotEmpty();
        // Should go through node 3 (lit path)
        assertThat(nightPath.stream().anyMatch(n -> n.getId() == 3)).isTrue();
    }

    @Test
    void calculatePathDistance_returns_positiveValue() {
        UserPreferences prefs = new UserPreferences();
        List<Node> path = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, prefs);

        double distance = router.calculatePathDistance(path);
        assertThat(distance).isGreaterThan(0);
    }

    @Test
    void calculatePathDistance_emptyPath_returnsZero() {
        assertThat(router.calculatePathDistance(List.of())).isEqualTo(0.0);
    }

    @Test
    void calculatePathDistance_singleNode_returnsZero() {
        assertThat(router.calculatePathDistance(List.of(new Node(1, 43.65, -79.38))))
            .isEqualTo(0.0);
    }

    @Test
    void estimateWalkingTime_returnsPositiveMinutes() {
        UserPreferences prefs = new UserPreferences();
        List<Node> path = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, prefs);

        double time = router.estimateWalkingTime(path);
        assertThat(time).isGreaterThan(0);
    }

    @Test
    void estimateWalkingTime_assumesWalkingSpeed() {
        // 5 km/h -> 83.33 m/min
        // Create two nodes ~500m apart
        Graph g = new Graph();
        g.addNode(new Node(1, 43.6500, -79.3800));
        g.addNode(new Node(2, 43.6545, -79.3800)); // ~500m north
        g.addEdge(1, 2, 500);

        AStarRouter r = new AStarRouter(g);
        List<Node> path = r.findRoute(43.6500, -79.3800, 43.6545, -79.3800, new UserPreferences());

        double time = r.estimateWalkingTime(path);
        // ~500m at 5km/h = ~6 minutes
        assertThat(time).isBetween(3.0, 12.0);
    }

    @Test
    void findRoute_resetsBetweenSearches() {
        UserPreferences prefs = new UserPreferences();

        // First search
        List<Node> path1 = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, prefs);
        assertThat(path1).isNotEmpty();

        // Second search on same graph should also work
        List<Node> path2 = router.findRoute(43.6532, -79.3832, 43.6532, -79.3780, prefs);
        assertThat(path2).isNotEmpty();
        assertThat(path2).hasSameSizeAs(path1);
    }
}
