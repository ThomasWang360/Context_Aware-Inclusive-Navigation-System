package com.group2.navigation.algorithm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Road network graph built from OpenStreetMap data.
 *
 * Supports loading from Overpass API JSON exports and provides a grid-based
 * spatial index for efficient nearest-node lookups.
 */
public class Graph {

    private Map<Long, Node> nodes;

    // Grid-based spatial index: key = "latBucket,lngBucket" -> nodes in that cell
    private Map<String, List<Node>> spatialIndex;

    // ~111 m latitude, ~80 m longitude at Toronto's latitude
    private static final double GRID_SIZE = 0.001;

    public Graph() {
        this.nodes = new HashMap<>();
        this.spatialIndex = new HashMap<>();
    }

    /**
     * Add a node to the graph and spatial index.
     */
    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        String key = gridKey(node.getLat(), node.getLng());
        spatialIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
    }

    /**
     * Get a node by its OSM ID.
     */
    public Node getNode(long id) {
        return nodes.get(id);
    }

    /**
     * Add a bidirectional edge between two nodes (basic version).
     */
    public void addEdge(long sourceId, long targetId, double distance) {
        Node source = nodes.get(sourceId);
        Node target = nodes.get(targetId);

        if (source != null && target != null) {
            source.addEdge(new Edge(source, target, distance));
            target.addEdge(new Edge(target, source, distance));
        }
    }

    /**
     * Add a bidirectional edge with context attributes parsed from OSM tags.
     */
    public void addEdge(long sourceId, long targetId, double distance,
                        boolean wheelchair, String surface, boolean lit) {
        Node source = nodes.get(sourceId);
        Node target = nodes.get(targetId);

        if (source != null && target != null) {
            Edge fwd = new Edge(source, target, distance);
            fwd.setWheelchairAccessible(wheelchair);
            fwd.setSurfaceType(surface);
            fwd.setLit(lit);
            source.addEdge(fwd);

            Edge rev = new Edge(target, source, distance);
            rev.setWheelchairAccessible(wheelchair);
            rev.setSurfaceType(surface);
            rev.setLit(lit);
            target.addEdge(rev);
        }
    }

    /**
     * Find the nearest graph node to a given coordinate using the spatial index.
     *
     * Searches in expanding rings around the grid cell containing (lat, lng).
     * After finding a candidate, searches two additional rings to guarantee
     * no closer node exists in an adjacent cell.
     */
    public Node findNearestNode(double lat, double lng) {
        if (nodes.isEmpty()) return null;

        if (spatialIndex.isEmpty()) {
            return findNearestNodeLinear(lat, lng);
        }

        int cLat = (int) Math.floor(lat / GRID_SIZE);
        int cLng = (int) Math.floor(lng / GRID_SIZE);

        Node nearest = null;
        double minDist = Double.MAX_VALUE;
        int extraRings = -1;

        for (int r = 0; r <= 50 && extraRings < 2; r++) {
            for (int dLat = -r; dLat <= r; dLat++) {
                for (int dLng = -r; dLng <= r; dLng++) {
                    // Only examine cells on this ring's border (skip interior)
                    if (r > 0 && Math.abs(dLat) < r && Math.abs(dLng) < r) continue;

                    String key = (cLat + dLat) + "," + (cLng + dLng);
                    List<Node> cell = spatialIndex.get(key);
                    if (cell == null) continue;

                    for (Node node : cell) {
                        double d = haversineDistance(lat, lng, node.getLat(), node.getLng());
                        if (d < minDist) {
                            minDist = d;
                            nearest = node;
                        }
                    }
                }
            }

            if (nearest != null) extraRings++;
        }

        return nearest;
    }

    private Node findNearestNodeLinear(double lat, double lng) {
        Node nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Node node : nodes.values()) {
            double d = haversineDistance(lat, lng, node.getLat(), node.getLng());
            if (d < minDist) {
                minDist = d;
                nearest = node;
            }
        }
        return nearest;
    }

    private String gridKey(double lat, double lng) {
        return (int) Math.floor(lat / GRID_SIZE) + "," + (int) Math.floor(lng / GRID_SIZE);
    }

    /**
     * Haversine distance between two points in meters.
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

    public int size() {
        return nodes.size();
    }

    public Iterable<Node> getAllNodes() {
        return nodes.values();
    }

    /**
     * Keep only the largest connected component and discard all smaller
     * disconnected fragments.  This guarantees that a route exists between
     * any two nodes remaining in the graph.
     */
    public void retainLargestComponent() {
        if (nodes.isEmpty()) return;

        // BFS to find connected components
        Set<Long> visited = new HashSet<>();
        List<Set<Long>> components = new ArrayList<>();

        for (Node node : nodes.values()) {
            if (visited.contains(node.getId())) continue;

            Set<Long> component = new HashSet<>();
            Queue<Node> queue = new LinkedList<>();
            queue.add(node);
            visited.add(node.getId());

            while (!queue.isEmpty()) {
                Node current = queue.poll();
                component.add(current.getId());
                for (Edge edge : current.getEdges()) {
                    long neighborId = edge.getTarget().getId();
                    if (!visited.contains(neighborId)) {
                        visited.add(neighborId);
                        queue.add(edge.getTarget());
                    }
                }
            }
            components.add(component);
        }

        // Find the largest component
        Set<Long> largest = components.stream()
                .max(Comparator.comparingInt(Set::size))
                .orElse(Collections.emptySet());

        // Remove all nodes not in the largest component
        int removed = nodes.size() - largest.size();
        nodes.keySet().retainAll(largest);

        // Rebuild spatial index
        spatialIndex.clear();
        for (Node node : nodes.values()) {
            String key = gridKey(node.getLat(), node.getLng());
            spatialIndex.computeIfAbsent(key, k -> new ArrayList<>()).add(node);
        }

        System.out.println("[Graph] Retained largest component: " + largest.size()
                + " nodes, pruned " + removed + " disconnected nodes ("
                + components.size() + " components found)");
    }

    /**
     * Load a road network graph from an Overpass API JSON export.
     *
     * Two-pass parsing:
     *   1. Collect all OSM node coordinates (id -> [lat, lon])
     *   2. For each OSM way, create graph nodes/edges between consecutive node pairs,
     *      applying wheelchair/surface/lighting attributes from OSM tags.
     */
    public static Graph loadFromOSM(String filePath) {
        Graph graph = new Graph();

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(new File(filePath));
            JsonNode elements = root.get("elements");

            if (elements == null || !elements.isArray()) {
                System.err.println("Invalid OSM JSON: missing 'elements' array");
                return graph;
            }

            // Pass 1: collect every OSM node's coordinates
            Map<Long, double[]> nodeCoords = new HashMap<>();
            for (JsonNode elem : elements) {
                if ("node".equals(elem.get("type").asText())) {
                    long id = elem.get("id").asLong();
                    nodeCoords.put(id, new double[]{
                            elem.get("lat").asDouble(),
                            elem.get("lon").asDouble()
                    });
                }
            }

            System.out.println("[Graph] Parsed " + nodeCoords.size() + " OSM node coordinates");

            // Pass 2: build graph from ways
            int wayCount = 0;
            for (JsonNode elem : elements) {
                if (!"way".equals(elem.get("type").asText())) continue;

                JsonNode nodeRefs = elem.get("nodes");
                if (nodeRefs == null) continue;
                JsonNode tags = elem.has("tags") ? elem.get("tags") : null;

                // --- parse context attributes from OSM tags ---
                String highway = (tags != null && tags.has("highway"))
                        ? tags.get("highway").asText() : "";

                boolean wheelchair = !"steps".equals(highway);
                if (tags != null && tags.has("wheelchair")) {
                    wheelchair = !"no".equals(tags.get("wheelchair").asText());
                }

                String surface = "paved";
                if (tags != null && tags.has("surface")) {
                    surface = tags.get("surface").asText();
                }

                boolean lit = true; // urban default
                if (tags != null && tags.has("lit")) {
                    lit = !"no".equals(tags.get("lit").asText());
                }

                // --- create edges between consecutive nodes in the way ---
                for (int i = 0; i < nodeRefs.size() - 1; i++) {
                    long id1 = nodeRefs.get(i).asLong();
                    long id2 = nodeRefs.get(i + 1).asLong();

                    double[] c1 = nodeCoords.get(id1);
                    double[] c2 = nodeCoords.get(id2);
                    if (c1 == null || c2 == null) continue;

                    if (graph.getNode(id1) == null) {
                        graph.addNode(new Node(id1, c1[0], c1[1]));
                    }
                    if (graph.getNode(id2) == null) {
                        graph.addNode(new Node(id2, c2[0], c2[1]));
                    }

                    double dist = haversineDistance(c1[0], c1[1], c2[0], c2[1]);
                    graph.addEdge(id1, id2, dist, wheelchair, surface, lit);
                }

                wayCount++;
            }

            System.out.println("[Graph] Built graph: " + graph.size()
                    + " nodes from " + wayCount + " ways");

        } catch (IOException e) {
            System.err.println("[Graph] Error loading OSM data from " + filePath
                    + ": " + e.getMessage());
        }

        return graph;
    }
}
