package com.group2.navigation.algorithm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NodeTest {

    @Test
    void constructor_setsFields() {
        Node node = new Node(42, 43.6532, -79.3832);

        assertThat(node.getId()).isEqualTo(42);
        assertThat(node.getLat()).isEqualTo(43.6532);
        assertThat(node.getLng()).isEqualTo(-79.3832);
        assertThat(node.getEdges()).isEmpty();
    }

    @Test
    void defaults_maxScoresAndNullParent() {
        Node node = new Node(1, 43.65, -79.38);

        assertThat(node.getGScore()).isEqualTo(Double.MAX_VALUE);
        assertThat(node.getFScore()).isEqualTo(Double.MAX_VALUE);
        assertThat(node.getParent()).isNull();
    }

    @Test
    void addEdge_addsToList() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);

        Edge edge = new Edge(a, b, 100);
        a.addEdge(edge);

        assertThat(a.getEdges()).hasSize(1);
        assertThat(a.getEdges().get(0).getTarget()).isSameAs(b);
    }

    @Test
    void addEdge_multipleEdges() {
        Node a = new Node(1, 43.65, -79.38);
        Node b = new Node(2, 43.66, -79.37);
        Node c = new Node(3, 43.67, -79.36);

        a.addEdge(new Edge(a, b, 100));
        a.addEdge(new Edge(a, c, 200));

        assertThat(a.getEdges()).hasSize(2);
    }

    @Test
    void reset_clearsAStarState() {
        Node node = new Node(1, 43.65, -79.38);
        Node parent = new Node(0, 43.64, -79.39);

        node.setGScore(5.0);
        node.setFScore(10.0);
        node.setParent(parent);

        node.reset();

        assertThat(node.getGScore()).isEqualTo(Double.MAX_VALUE);
        assertThat(node.getFScore()).isEqualTo(Double.MAX_VALUE);
        assertThat(node.getParent()).isNull();
    }

    @Test
    void setGScore_and_setFScore() {
        Node node = new Node(1, 43.65, -79.38);

        node.setGScore(42.5);
        node.setFScore(99.9);

        assertThat(node.getGScore()).isEqualTo(42.5);
        assertThat(node.getFScore()).isEqualTo(99.9);
    }

    @Test
    void parentChain_forPathReconstruction() {
        Node n1 = new Node(1, 43.65, -79.38);
        Node n2 = new Node(2, 43.66, -79.37);
        Node n3 = new Node(3, 43.67, -79.36);

        n3.setParent(n2);
        n2.setParent(n1);

        assertThat(n3.getParent()).isSameAs(n2);
        assertThat(n3.getParent().getParent()).isSameAs(n1);
        assertThat(n1.getParent()).isNull();
    }
}
