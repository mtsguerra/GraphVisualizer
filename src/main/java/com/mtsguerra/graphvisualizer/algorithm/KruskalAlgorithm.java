package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.StepAction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Kruskal's minimum spanning tree (a spanning forest if the graph is disconnected).
 * Edges are taken cheapest first and kept when they join two different components.
 * Direction is ignored, and startNode/endNode are not used.
 * <p>
 * Every accepted edge emits two PATH steps, one per endpoint, the second carrying
 * the edge and the running total as its distance. Rejected edges emit nothing.
 * The result has no path; totalDistance is the weight of the tree.
 */
@Component
public class KruskalAlgorithm implements GraphAlgorithm {

    @Override
    public AlgorithmResult run(GraphInput input) {
        Graph graph = Graph.from(input);

        List<Edge> sorted = graph.edges().stream().sorted(Comparator.comparingDouble(Edge::weight)).toList();
        UnionFind components = new UnionFind(graph.nodeIds());
        StepRecorder recorder = new StepRecorder();
        double total = 0;
        int accepted = 0;

        for (Edge edge : sorted) {
            if (!components.union(edge.from(), edge.to())) {
                continue;
            }
            total += edge.weight();
            accepted++;
            recorder.add(edge.from(), StepAction.PATH, null, total);
            recorder.add(edge.to(), StepAction.PATH, edge, total);
            if (accepted == graph.nodeIds().size() - 1) {
                break;
            }
        }
        return new AlgorithmResult("kruskal", recorder.steps(), null, total);
    }
}
