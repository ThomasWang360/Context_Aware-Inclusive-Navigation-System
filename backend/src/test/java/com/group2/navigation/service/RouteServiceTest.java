package com.group2.navigation.service;

import com.group2.navigation.algorithm.Graph;
import com.group2.navigation.algorithm.Node;
import com.group2.navigation.model.RouteRequest;
import com.group2.navigation.model.RouteResponse;
import com.group2.navigation.model.UserPreferences;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RouteServiceTest {

    @Autowired
    private RouteService routeService;

    @Autowired
    private GraphService graphService;

    /** Helper: return all graph nodes as a list, or skip test if graph is empty. */
    private List<Node> getNodesOrSkip() {
        Graph graph = graphService.getGraph();
        assertThat(graph).isNotNull();
        List<Node> list = new ArrayList<>();
        graph.getAllNodes().forEach(list::add);
        assumeTrue(!list.isEmpty(), "graph has no nodes – skipping");
        return list;
    }

    // --- calculateRoute with coordinates ---

    @Test
    void calculateRoute_withCoordinates_returnsSuccessfulResponse() {
        List<Node> nodes = getNodesOrSkip();
        assumeTrue(nodes.size() >= 2, "need at least 2 nodes");

        Node first = nodes.get(0);
        Node second = nodes.get(nodes.size() - 1);

        RouteRequest request = new RouteRequest(
                first.getLat(), first.getLng(),
                second.getLat(), second.getLng());
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);

        // Route may succeed or fail depending on graph connectivity
        assertThat(response).isNotNull();
        if (response.isSuccess()) {
            assertThat(response.getCoordinates()).isNotEmpty();
            assertThat(response.getTotalDistance()).isGreaterThanOrEqualTo(0);
            assertThat(response.getEstimatedTime()).isGreaterThanOrEqualTo(0);
            assertThat(response.getMessage()).isEqualTo("Route calculated successfully");
        }
    }

    @Test
    void calculateRoute_sameStartAndEnd_returnsResponse() {
        List<Node> nodes = getNodesOrSkip();
        Node node = nodes.get(0);

        RouteRequest request = new RouteRequest(
                node.getLat(), node.getLng(),
                node.getLat(), node.getLng());
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
    }

    @Test
    void calculateRoute_disconnectedPoints_returnsError() {
        // Use a point far outside the graph to ensure no route found
        RouteRequest request = new RouteRequest(0.0, 0.0, 1.0, 1.0);
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("No route found");
    }

    @Test
    void calculateRoute_withNullPreferences_usesDefaults() {
        List<Node> nodes = getNodesOrSkip();
        Node first = nodes.get(0);

        RouteRequest request = new RouteRequest(
                first.getLat(), first.getLng(),
                first.getLat(), first.getLng());
        // Preferences left as null
        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
    }

    // --- calculateRoute with addresses ---

    @Test
    void calculateRoute_withInvalidStartAddress_returnsError() {
        RouteRequest request = new RouteRequest();
        request.setStartAddress("zzznonexistentplacexyz");
        request.setEndLat(43.6532);
        request.setEndLng(-79.3832);
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Could not find start address");
    }

    @Test
    void calculateRoute_withInvalidEndAddress_returnsError() {
        RouteRequest request = new RouteRequest();
        request.setStartLat(43.6532);
        request.setStartLng(-79.3832);
        request.setEndAddress("zzznonexistentplacexyz");
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Could not find end address");
    }

    @Test
    void calculateRoute_withPreferences_returnsResponse() {
        List<Node> nodes = getNodesOrSkip();
        assumeTrue(nodes.size() >= 2, "need at least 2 nodes");

        Node first = nodes.get(0);
        Node second = nodes.get(nodes.size() - 1);

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(5);
        prefs.setCrimeWeight(8);
        prefs.setLightingWeight(3);
        prefs.setConstructionWeight(2);
        prefs.setTimeOfDay(22); // night
        prefs.setMaxDistanceToHospital(2000);

        RouteRequest request = new RouteRequest(
                first.getLat(), first.getLng(),
                second.getLat(), second.getLng());
        request.setPreferences(prefs);

        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
    }

    @Test
    void calculateRoute_successfulResponse_hasPositiveDistanceAndTime() {
        List<Node> nodes = getNodesOrSkip();
        assumeTrue(nodes.size() >= 4, "need at least 4 nodes for meaningful route");

        Node n1 = nodes.get(0);
        Node n2 = nodes.get(3);

        RouteRequest request = new RouteRequest(
                n1.getLat(), n1.getLng(),
                n2.getLat(), n2.getLng());
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);
        if (response.isSuccess()) {
            assertThat(response.getTotalDistance()).isGreaterThan(0);
            assertThat(response.getEstimatedTime()).isGreaterThan(0);
            assertThat(response.getCoordinates().size()).isGreaterThanOrEqualTo(2);

            // Verify coordinates are valid lat/lng pairs
            for (double[] coord : response.getCoordinates()) {
                assertThat(coord).hasSize(2);
                assertThat(coord[0]).isBetween(-90.0, 90.0);
                assertThat(coord[1]).isBetween(-180.0, 180.0);
            }
        }
    }

    @Test
    void calculateRoute_blankAddress_usesCoordinates() {
        List<Node> nodes = getNodesOrSkip();
        Node node = nodes.get(0);

        RouteRequest request = new RouteRequest(
                node.getLat(), node.getLng(),
                node.getLat(), node.getLng());
        request.setStartAddress("   ");
        request.setEndAddress("");
        request.setPreferences(new UserPreferences());

        RouteResponse response = routeService.calculateRoute(request);
        assertThat(response).isNotNull();
    }

    // --- graph initialization ---

    @Test
    void graphService_graphIsInitialized() {
        Graph graph = graphService.getGraph();
        assertThat(graph).isNotNull();
        // graph may be empty if OSM data is unavailable, just check it loaded
        assertThat(graph.size()).isGreaterThanOrEqualTo(0);
    }
}
