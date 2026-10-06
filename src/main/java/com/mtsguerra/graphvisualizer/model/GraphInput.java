package com.mtsguerra.graphvisualizer.model;

import java.util.List;

/**
 * Request body for every algorithm endpoint.
 *
 * @param endNode  optional for BFS/DFS (search stops early when reached),
 *                 required for Dijkstra and A*
 * @param directed when false (the default), every edge works in both directions
 */
public record GraphInput(
        List<Node> nodes,
        List<Edge> edges,
        Integer startNode,
        Integer endNode,
        boolean directed) {

    public GraphInput {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
        edges = edges == null ? List.of() : List.copyOf(edges);
    }

    public GraphInput(List<Node> nodes, List<Edge> edges, Integer startNode, Integer endNode) {
        this(nodes, edges, startNode, endNode, false);
    }
}
