package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.Step;
import com.mtsguerra.graphvisualizer.model.StepAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Collects animation steps, assigning each one the next timestamp. */
final class StepRecorder {

    private final List<Step> steps = new ArrayList<>();

    void add(int nodeId, StepAction action) {
        add(nodeId, action, null, null);
    }

    void add(int nodeId, StepAction action, Edge edge) {
        add(nodeId, action, edge, null);
    }

    void add(int nodeId, StepAction action, Edge edge, Double distance) {
        steps.add(new Step(nodeId, action, steps.size(), edge, distance));
    }

    /**
     * Walks {@code parentEdge} back from {@code end} to {@code start} and records a
     * PATH step for every node on the way, in start-to-end order.
     *
     * @param parentEdge for each reached node, the edge it was reached through
     * @param distances  optional; when given, each PATH step carries the node's distance
     * @return the path, or an empty list if {@code end} was never reached
     */
    List<Integer> recordPath(int start, int end, Map<Integer, Edge> parentEdge, Map<Integer, Double> distances) {
        if (start != end && !parentEdge.containsKey(end)) {
            return List.of();
        }
        List<Integer> path = new ArrayList<>();
        for (int node = end; node != start; node = parentEdge.get(node).from()) {
            path.add(node);
        }
        path.add(start);
        Collections.reverse(path);

        for (int node : path) {
            Edge edge = node == start ? null : parentEdge.get(node);
            add(node, StepAction.PATH, edge, distances == null ? null : distances.get(node));
        }
        return path;
    }

    List<Step> steps() {
        return List.copyOf(steps);
    }
}
