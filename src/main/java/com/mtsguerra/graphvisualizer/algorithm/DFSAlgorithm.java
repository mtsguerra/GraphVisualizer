package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.StepAction;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Depth-first search, iterative so deep graphs can't overflow the call stack.
 * Emits EXPLORING when an edge to an unseen node is followed, CURRENT when that
 * node is entered, and VISITED when all its neighbors are done (backtracking).
 * When an endNode is given, the search stops as soon as it is entered.
 */
@Component
public class DFSAlgorithm implements GraphAlgorithm {

    private record Frame(int node, Iterator<Graph.Neighbor> neighbors) {
    }

    @Override
    public AlgorithmResult run(GraphInput input) {
        Graph graph = Graph.from(input);
        int start = graph.requireNode(input.startNode(), "startNode");
        Integer end = graph.optionalNode(input.endNode(), "endNode");

        StepRecorder recorder = new StepRecorder();
        Map<Integer, Edge> parentEdge = new HashMap<>();
        Set<Integer> visited = new HashSet<>(Set.of(start));
        Deque<Frame> stack = new ArrayDeque<>();

        recorder.add(start, StepAction.CURRENT);
        stack.push(new Frame(start, graph.neighbors(start).iterator()));
        boolean reachedEnd = end != null && start == end;

        while (!stack.isEmpty() && !reachedEnd) {
            Frame top = stack.peek();
            if (!top.neighbors().hasNext()) {
                stack.pop();
                recorder.add(top.node(), StepAction.VISITED);
                continue;
            }
            Graph.Neighbor neighbor = top.neighbors().next();
            if (visited.add(neighbor.node())) {
                parentEdge.put(neighbor.node(), neighbor.edge());
                recorder.add(neighbor.node(), StepAction.EXPLORING, neighbor.edge());
                recorder.add(neighbor.node(), StepAction.CURRENT, neighbor.edge());
                stack.push(new Frame(neighbor.node(), graph.neighbors(neighbor.node()).iterator()));
                reachedEnd = end != null && neighbor.node() == end;
            }
        }
        if (reachedEnd) {
            recorder.add(end, StepAction.VISITED);
        }

        List<Integer> path = end == null ? null : recorder.recordPath(start, end, parentEdge, null);
        return new AlgorithmResult("dfs", recorder.steps(), path, null);
    }
}
