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
 * Dijkstra's shortest path from startNode to endNode. Steps carry the best
 * known distance: EXPLORING is emitted each time a node's distance improves.
 */
@Component
public class DijkstraAlgorithm implements GraphAlgorithm {

    private record Entry(int node, double distance) {
    }

    @Override
    public AlgorithmResult run(GraphInput input) {
        Graph graph = Graph.from(input);
        int start = graph.requireNode(input.startNode(), "startNode");
        int end = graph.requireNode(input.endNode(), "endNode");
        graph.requireNonNegativeWeights();

        StepRecorder recorder = new StepRecorder();
        Map<Integer, Double> distances = new HashMap<>(Map.of(start, 0.0));
        Map<Integer, Edge> parentEdge = new HashMap<>();
        Set<Integer> settled = new HashSet<>();
        PriorityQueue<Entry> queue = new PriorityQueue<>(Comparator.comparingDouble(Entry::distance));
        queue.add(new Entry(start, 0.0));
        recorder.add(start, StepAction.EXPLORING, null, 0.0);

        while (!queue.isEmpty()) {
            Entry entry = queue.poll();
            int current = entry.node();
            // Stale entry: this node was already settled through a shorter route.
            if (!settled.add(current)) {
                continue;
            }
            recorder.add(current, StepAction.CURRENT, parentEdge.get(current), entry.distance());

            if (current != end) {
                for (Graph.Neighbor neighbor : graph.neighbors(current)) {
                    if (settled.contains(neighbor.node())) {
                        continue;
                    }
                    double candidate = entry.distance() + neighbor.edge().weight();
                    if (candidate < distances.getOrDefault(neighbor.node(), Double.POSITIVE_INFINITY)) {
                        distances.put(neighbor.node(), candidate);
                        parentEdge.put(neighbor.node(), neighbor.edge());
                        queue.add(new Entry(neighbor.node(), candidate));
                        recorder.add(neighbor.node(), StepAction.EXPLORING, neighbor.edge(), candidate);
                    }
                }
            }
            recorder.add(current, StepAction.VISITED, null, entry.distance());
            if (current == end) {
                break;
            }
        }

        List<Integer> path = recorder.recordPath(start, end, parentEdge, distances);
        Double total = path.isEmpty() ? null : distances.get(end);
        return new AlgorithmResult("dijkstra", recorder.steps(), path, total);
    }
}
