package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
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
 * Widest (maximum-bottleneck) path from startNode to endNode: edge weights are
 * capacities, and a path is as good as its smallest edge. Same shape as Dijkstra
 * but with a max-heap and {@code min} in place of {@code +}. Step distances carry
 * the best bottleneck known for the node (the start node has none, so it is null),
 * and totalDistance is the bottleneck of the returned path.
 */
@Component
public class WidestPathAlgorithm implements GraphAlgorithm {

    @Override
    public AlgorithmResult run(GraphInput input) {
        Graph graph = Graph.from(input);
        int start = graph.requireNode(input.startNode(), "startNode");
        int end = graph.requireNode(input.endNode(), "endNode");
        graph.requireNonNegativeWeights();

        StepRecorder recorder = new StepRecorder();
        // The start node is unbounded, so it is left out of this map on purpose.
        Map<Integer, Double> widths = new HashMap<>();
        Map<Integer, Edge> parentEdge = new HashMap<>();
        Set<Integer> settled = new HashSet<>();
        PriorityQueue<FrontierNode> queue =
                new PriorityQueue<>(Comparator.comparingDouble(FrontierNode::cost).reversed());
        queue.add(new FrontierNode(start, Double.POSITIVE_INFINITY));
        recorder.add(start, StepAction.EXPLORING);

        while (!queue.isEmpty()) {
            FrontierNode entry = queue.poll();
            int current = entry.node();
            // Stale entry: this node was already settled through a wider route.
            if (!settled.add(current)) {
                continue;
            }
            recorder.add(current, StepAction.CURRENT, parentEdge.get(current), widths.get(current));

            if (current != end) {
                for (Graph.Neighbor neighbor : graph.neighbors(current)) {
                    if (settled.contains(neighbor.node())) {
                        continue;
                    }
                    double candidate = Math.min(entry.cost(), neighbor.edge().weight());
                    if (candidate > widths.getOrDefault(neighbor.node(), Double.NEGATIVE_INFINITY)) {
                        widths.put(neighbor.node(), candidate);
                        parentEdge.put(neighbor.node(), neighbor.edge());
                        queue.add(new FrontierNode(neighbor.node(), candidate));
                        recorder.add(neighbor.node(), StepAction.EXPLORING, neighbor.edge(), candidate);
                    }
                }
            }
            recorder.add(current, StepAction.VISITED, null, widths.get(current));
            if (current == end) {
                break;
            }
        }

        List<Integer> path = recorder.recordPath(start, end, parentEdge, widths);
        Double bottleneck = path.isEmpty() ? null : widths.get(end);
        return new AlgorithmResult("widest", recorder.steps(), path, bottleneck);
    }
}
