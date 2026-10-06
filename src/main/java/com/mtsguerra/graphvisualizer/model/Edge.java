package com.mtsguerra.graphvisualizer.model;

/**
 * A connection between two nodes. Weight is used by Dijkstra and A*;
 * it defaults to 1 when omitted, so BFS/DFS inputs don't need it.
 */
public record Edge(int from, int to, Double weight) {

    public Edge {
        if (weight == null) {
            weight = 1.0;
        }
    }

    public Edge(int from, int to) {
        this(from, to, null);
    }

    /** The same edge traversed in the opposite direction. */
    public Edge reversed() {
        return new Edge(to, from, weight);
    }
}
