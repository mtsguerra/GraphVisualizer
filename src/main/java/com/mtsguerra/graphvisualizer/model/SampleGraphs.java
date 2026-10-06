package com.mtsguerra.graphvisualizer.model;

import java.util.List;

/** Hardcoded graphs for manual testing and unit tests. */
public final class SampleGraphs {

    private SampleGraphs() {
    }

    /**
     * Six-node undirected weighted graph, start A (0), end F (5).
     * <pre>
     *        B ---5--- D
     *      / |       / | \
     *     4  1     8   2  6
     *    /   |   /     |   \
     *   A -2-- C --10-- E -3- F
     * </pre>
     * Fewest edges (BFS):    A-B-D-F,        cost 15
     * Cheapest (Dijkstra/A*): A-C-B-D-E-F,   cost 13
     */
    public static GraphInput weighted() {
        List<Node> nodes = List.of(
                new Node(0, "A", 100, 200),
                new Node(1, "B", 250, 100),
                new Node(2, "C", 250, 300),
                new Node(3, "D", 450, 100),
                new Node(4, "E", 450, 300),
                new Node(5, "F", 600, 200));
        List<Edge> edges = List.of(
                new Edge(0, 1, 4.0),
                new Edge(0, 2, 2.0),
                new Edge(1, 2, 1.0),
                new Edge(1, 3, 5.0),
                new Edge(2, 4, 10.0),
                new Edge(2, 3, 8.0),
                new Edge(3, 4, 2.0),
                new Edge(3, 5, 6.0),
                new Edge(4, 5, 3.0));
        return new GraphInput(nodes, edges, 0, 5);
    }
}
