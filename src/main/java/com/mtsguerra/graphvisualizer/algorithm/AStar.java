package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.Node;
import com.mtsguerra.graphvisualizer.model.StepAction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A* search from startNode to endNode, guided by the straight-line distance
 * between node coordinates. Step distances are the cost so far (g), not g + h.
 */
@Component
public class AStar implements GraphAlgorithm {

    private record Entry(int node, double g, double f) {
    }

    @Override
    public AlgorithmResult run(GraphInput input) {
        Graph graph = Graph.from(input);
        int start = graph.requireNode(input.startNode(), "startNode");
        int end = graph.requireNode(input.endNode(), "endNode");
        graph.requireNonNegativeWeights();

        Node goal = graph.node(end);
        double scale = heuristicScale(graph);

        StepRecorder recorder = new StepRecorder();
        Map<Integer, Double> costs = new HashMap<>(Map.of(start, 0.0));
        Map<Integer, Edge> parentEdge = new HashMap<>();
        Set<Integer> closed = new HashSet<>();
        PriorityQueue<Entry> open = new PriorityQueue<>(
                Comparator.comparingDouble(Entry::f).thenComparingDouble(e -> -e.g()));
        open.add(new Entry(start, 0.0, scale * distance(graph.node(start), goal)));
        recorder.add(start, StepAction.EXPLORING, null, 0.0);

        while (!open.isEmpty()) {
            Entry entry = open.poll();
            int current = entry.node();
            if (!closed.add(current)) {
                continue;
            }
            recorder.add(current, StepAction.CURRENT, parentEdge.get(current), entry.g());

            if (current != end) {
                for (Graph.Neighbor neighbor : graph.neighbors(current)) {
                    if (closed.contains(neighbor.node())) {
                        continue;
                    }
                    double g = entry.g() + neighbor.edge().weight();
                    if (g < costs.getOrDefault(neighbor.node(), Double.POSITIVE_INFINITY)) {
                        costs.put(neighbor.node(), g);
                        parentEdge.put(neighbor.node(), neighbor.edge());
                        double h = scale * distance(graph.node(neighbor.node()), goal);
                        open.add(new Entry(neighbor.node(), g, g + h));
                        recorder.add(neighbor.node(), StepAction.EXPLORING, neighbor.edge(), g);
                    }
                }
            }
            recorder.add(current, StepAction.VISITED, null, entry.g());
            if (current == end) {
                break;
            }
        }

        List<Integer> path = recorder.recordPath(start, end, parentEdge, costs);
        Double total = path.isEmpty() ? null : costs.get(end);
        return new AlgorithmResult("astar", recorder.steps(), path, total);
    }

    /**
     * Edge weights and canvas coordinates use unrelated units, so raw Euclidean
     * distance could overestimate the remaining cost and make A* return a
     * non-shortest path. Scaling by the smallest weight-per-unit-length of any
     * edge guarantees h never overestimates (admissible and consistent), while
     * still pointing the search toward the goal.
     */
    private static double heuristicScale(Graph graph) {
        double scale = Double.POSITIVE_INFINITY;
        for (Edge edge : graph.edges()) {
            double length = distance(graph.node(edge.from()), graph.node(edge.to()));
            if (length > 0) {
                scale = Math.min(scale, edge.weight() / length);
            }
        }
        return Double.isInfinite(scale) ? 0.0 : scale;
    }

    private static double distance(Node a, Node b) {
        return Math.hypot(a.x() - b.x(), a.y() - b.y());
    }
}
