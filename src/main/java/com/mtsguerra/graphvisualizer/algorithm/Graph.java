package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Validated adjacency-list view of a {@link GraphInput}. Neighbors keep the
 * order in which edges were given, so every run is deterministic.
 */
public final class Graph {

    /** A reachable neighbor and the edge used to reach it (oriented current -> node). */
    public record Neighbor(int node, Edge edge) {
    }

    private final Map<Integer, Node> nodes = new LinkedHashMap<>();
    private final Map<Integer, List<Neighbor>> adjacency = new LinkedHashMap<>();
    private final List<Edge> edges;

    private Graph(GraphInput input) {
        for (Node node : input.nodes()) {
            if (nodes.putIfAbsent(node.id(), node) != null) {
                throw new InvalidGraphException("Duplicate node id: " + node.id());
            }
            adjacency.put(node.id(), new ArrayList<>());
        }
        for (Edge edge : input.edges()) {
            requireNode(edge.from(), "edge.from");
            requireNode(edge.to(), "edge.to");
            adjacency.get(edge.from()).add(new Neighbor(edge.to(), edge));
            if (!input.directed() && edge.from() != edge.to()) {
                adjacency.get(edge.to()).add(new Neighbor(edge.from(), edge.reversed()));
            }
        }
        this.edges = input.edges();
    }

    public static Graph from(GraphInput input) {
        if (input == null) {
            throw new InvalidGraphException("Request body is required");
        }
        return new Graph(input);
    }

    public List<Neighbor> neighbors(int nodeId) {
        return adjacency.get(nodeId);
    }

    public Node node(int nodeId) {
        return nodes.get(nodeId);
    }

    public Collection<Integer> nodeIds() {
        return nodes.keySet();
    }

    public Collection<Edge> edges() {
        return edges;
    }

    public int requireNode(Integer nodeId, String field) {
        if (nodeId == null) {
            throw new InvalidGraphException(field + " is required");
        }
        if (!nodes.containsKey(nodeId)) {
            throw new InvalidGraphException(field + " refers to unknown node " + nodeId);
        }
        return nodeId;
    }

    /** Like {@link #requireNode} but allows null (field not provided). */
    public Integer optionalNode(Integer nodeId, String field) {
        return nodeId == null ? null : requireNode(nodeId, field);
    }

    /** Dijkstra and A* give wrong answers with negative weights, so reject them up front. */
    public void requireNonNegativeWeights() {
        for (Edge edge : edges) {
            if (edge.weight() < 0) {
                throw new InvalidGraphException(
                        "Negative weight " + edge.weight() + " on edge " + edge.from() + " -> " + edge.to());
            }
        }
    }
}
