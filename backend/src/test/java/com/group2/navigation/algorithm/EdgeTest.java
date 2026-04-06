package com.group2.navigation.algorithm;

import com.group2.navigation.model.UserPreferences;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EdgeTest {

    @Test
    void defaultEdge_allAccessible() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);

        assertThat(edge.isWheelchairAccessible()).isTrue();
        assertThat(edge.isLit()).isTrue();
        assertThat(edge.getCrimeScore()).isEqualTo(0);
        assertThat(edge.hasConstruction()).isFalse();
        assertThat(edge.getSurfaceType()).isEqualTo("paved");
        assertThat(edge.getDistance()).isEqualTo(100);
    }

    @Test
    void getWeightedCost_noPreferences_returnsBaseDistance() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 250);

        UserPreferences prefs = new UserPreferences();
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(250);
    }

    @Test
    void getWeightedCost_wheelchairPenalty_inaccessible() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setWheelchairAccessible(false);

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(10.0);

        // cost = 100 + 10 * 500 = 5100
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(5100);
    }

    @Test
    void getWeightedCost_wheelchairPenalty_accessible() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        // default wheelchair accessible

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(10.0);

        // No penalty since edge IS wheelchair accessible
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(100);
    }

    @Test
    void getWeightedCost_crimePenalty() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setCrimeScore(0.5);

        UserPreferences prefs = new UserPreferences();
        prefs.setCrimeWeight(8.0);

        // cost = 100 + 0.5 * 8 * 100 = 500
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(500);
    }

    @Test
    void getWeightedCost_lightingPenalty_night_unlit() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setLit(false);

        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(5.0);
        prefs.setTimeOfDay(22); // night

        // cost = 100 + 5 * 50 = 350
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(350);
    }

    @Test
    void getWeightedCost_lightingPenalty_day_unlit() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setLit(false);

        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(5.0);
        prefs.setTimeOfDay(14); // day

        // cost = 100 + 5 * 5 = 125
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(125);
    }

    @Test
    void getWeightedCost_lightingPenalty_litEdge_noPenalty() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        // default lit = true

        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(10.0);
        prefs.setTimeOfDay(22);

        assertThat(edge.getWeightedCost(prefs)).isEqualTo(100);
    }

    @Test
    void getWeightedCost_constructionPenalty() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setHasConstruction(true);

        UserPreferences prefs = new UserPreferences();
        prefs.setConstructionWeight(7.0);

        // cost = 100 + 7 * 80 = 660
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(660);
    }

    @Test
    void getWeightedCost_allPenaltiesCombined() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setWheelchairAccessible(false);
        edge.setCrimeScore(1.0);
        edge.setLit(false);
        edge.setHasConstruction(true);

        UserPreferences prefs = new UserPreferences();
        prefs.setWheelchairWeight(10.0);
        prefs.setCrimeWeight(10.0);
        prefs.setLightingWeight(10.0);
        prefs.setConstructionWeight(10.0);
        prefs.setTimeOfDay(22); // night

        // cost = 100 + 5000 + 1000 + 500 + 800 = 7400
        assertThat(edge.getWeightedCost(prefs)).isEqualTo(7400);
    }

    @Test
    void getWeightedCost_nightBoundary_hour20() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setLit(false);

        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(1.0);
        prefs.setTimeOfDay(20); // exactly 20 => night

        assertThat(edge.getWeightedCost(prefs)).isEqualTo(150); // 100 + 1*50
    }

    @Test
    void getWeightedCost_nightBoundary_hour5() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setLit(false);

        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(1.0);
        prefs.setTimeOfDay(5); // 5am => still night

        assertThat(edge.getWeightedCost(prefs)).isEqualTo(150);
    }

    @Test
    void getWeightedCost_dayBoundary_hour6() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);
        edge.setLit(false);

        UserPreferences prefs = new UserPreferences();
        prefs.setLightingWeight(1.0);
        prefs.setTimeOfDay(6); // 6am => day

        assertThat(edge.getWeightedCost(prefs)).isEqualTo(105); // 100 + 1*5
    }

    @Test
    void setters_updateValues() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 100);

        edge.setWheelchairAccessible(false);
        edge.setLit(false);
        edge.setCrimeScore(0.75);
        edge.setHasConstruction(true);
        edge.setSurfaceType("gravel");

        assertThat(edge.isWheelchairAccessible()).isFalse();
        assertThat(edge.isLit()).isFalse();
        assertThat(edge.getCrimeScore()).isEqualTo(0.75);
        assertThat(edge.hasConstruction()).isTrue();
        assertThat(edge.getSurfaceType()).isEqualTo("gravel");
    }

    @Test
    void edgeSource_and_target_returnCorrectNodes() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Edge edge = new Edge(a, b, 200);

        assertThat(edge.getSource()).isSameAs(a);
        assertThat(edge.getTarget()).isSameAs(b);
    }
}
