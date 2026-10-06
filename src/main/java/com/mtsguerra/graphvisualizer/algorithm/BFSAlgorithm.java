package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.StepAction;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Breadth-first search. When an endNode is given, the search stops once it is
 * dequeued and the returned path has the fewest possible edges.
 */
@Component
public class BFSAlgorithm implements GraphAlgorithm {

    @Override
    public AlgorithmResult run(GraphInput input) {
        Graph graph = Graph.from(input);
        int start = graph.requireNode(input.startNode(), "startNode");
        Integer end = graph.optionalNode(input.endNode(), "endNode");

        StepRecorder recorder = new StepRecorder();
        Map<Integer, Edge> parentEdge = new HashMap<>();
        Set<Integer> discovered = new HashSet<>(Set.of(start));
        Queue<Integer> queue = new ArrayDeque<>(List.of(start));
        recorder.add(start, StepAction.EXPLORING);

        while (!queue.isEmpty()) {
            int current = queue.poll();
            recorder.add(current, StepAction.CURRENT);
            boolean reachedEnd = end != null && current == end;

            if (!reachedEnd) {
                for (Graph.Neighbor neighbor : graph.neighbors(current)) {
                    if (discovered.add(neighbor.node())) {
                        parentEdge.put(neighbor.node(), neighbor.edge());
                        queue.add(neighbor.node());
                        recorder.add(neighbor.node(), StepAction.EXPLORING, neighbor.edge());
                    }
                }
            }
            recorder.add(current, StepAction.VISITED);
            if (reachedEnd) {
                break;
            }
        }

        List<Integer> path = end == null ? null : recorder.recordPath(start, end, parentEdge, null);
        return new AlgorithmResult("bfs", recorder.steps(), path, null);
    }
}
