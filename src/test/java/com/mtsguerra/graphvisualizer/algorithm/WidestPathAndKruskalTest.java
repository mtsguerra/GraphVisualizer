package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.Node;
import com.mtsguerra.graphvisualizer.model.SampleGraphs;
import com.mtsguerra.graphvisualizer.model.Step;
import com.mtsguerra.graphvisualizer.model.StepAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WidestPathAndKruskalTest {

    private final GraphInput sample = SampleGraphs.weighted();

    private static List<Node> nodes(int count) {
        return IntStream.range(0, count).mapToObj(i -> new Node(i, "N" + i, i * 10, 0)).toList();
    }

    @Test
    void widestPicksLargestBottleneckNotShortestRoute() {
        AlgorithmResult result = new WidestPathAlgorithm().run(sample);

        // A-B-D-F has smallest edge 4; the cheapest route A-C-B-D-E-F is limited to 1.
        assertThat(result.path()).containsExactly(0, 1, 3, 5);
        assertThat(result.totalDistance()).isEqualTo(4.0);
        assertThat(result.steps()).extracting(Step::timestamp)
                .containsExactlyElementsOf(IntStream.range(0, result.steps().size()).boxed().toList());
    }

    @Test
    void widestBeatsDirectNarrowEdge() {
        List<Edge> edges = List.of(new Edge(0, 2, 1.0), new Edge(0, 1, 8.0), new Edge(1, 2, 6.0));
        AlgorithmResult result = new WidestPathAlgorithm().run(new GraphInput(nodes(3), edges, 0, 2));

        assertThat(result.path()).containsExactly(0, 1, 2);
        assertThat(result.totalDistance()).isEqualTo(6.0);
    }

    @Test
    void widestEdgeCases() {
        GraphInput single = new GraphInput(nodes(1), List.of(), 0, 0);
        AlgorithmResult same = new WidestPathAlgorithm().run(single);
        assertThat(same.path()).containsExactly(0);
        assertThat(same.totalDistance()).isNull();

        GraphInput split = new GraphInput(nodes(3), List.of(new Edge(0, 1, 3.0)), 0, 2);
        AlgorithmResult unreachable = new WidestPathAlgorithm().run(split);
        assertThat(unreachable.path()).isEmpty();
        assertThat(unreachable.totalDistance()).isNull();

        GraphInput cycle = new GraphInput(nodes(3),
                List.of(new Edge(0, 1, 2.0), new Edge(1, 2, 2.0), new Edge(2, 0, 2.0)), 0, 2);
        assertThat(new WidestPathAlgorithm().run(cycle).path()).containsExactly(0, 2);

        GraphInput negative = new GraphInput(nodes(2), List.of(new Edge(0, 1, -1.0)), 0, 1);
        assertThatThrownBy(() -> new WidestPathAlgorithm().run(negative))
                .isInstanceOf(InvalidGraphException.class).hasMessageContaining("Negative weight");
    }

    @Test
    void widestRespectsDirection() {
        GraphInput input = new GraphInput(nodes(2), List.of(new Edge(1, 0, 5.0)), 0, 1, true);

        assertThat(new WidestPathAlgorithm().run(input).path()).isEmpty();
    }

    @Test
    void kruskalBuildsMinimumSpanningTree() {
        AlgorithmResult result = new KruskalAlgorithm().run(sample);

        // B-C 1, A-C 2, D-E 2, E-F 3, B-D 5; A-B (4) would close a cycle.
        assertThat(result.totalDistance()).isEqualTo(13.0);
        assertThat(result.path()).isNull();
        List<Edge> accepted = result.steps().stream().map(Step::edge).filter(e -> e != null).toList();
        assertThat(accepted).hasSize(5).doesNotContain(new Edge(0, 1, 4.0));
        assertThat(result.steps()).allMatch(s -> s.action() == StepAction.PATH);
        assertThat(result.steps().getLast().distance()).isEqualTo(13.0);
    }

    @Test
    void kruskalGivesForestForDisconnectedGraph() {
        List<Edge> edges = List.of(new Edge(0, 1, 4.0), new Edge(2, 3, 7.0));
        AlgorithmResult result = new KruskalAlgorithm().run(new GraphInput(nodes(4), edges, 0, null));

        assertThat(result.totalDistance()).isEqualTo(11.0);
        assertThat(result.steps()).hasSize(4);
    }

    @Test
    void kruskalSkipsCyclesSelfLoopsAndParallelEdges() {
        List<Edge> edges = List.of(new Edge(0, 0, 1.0), new Edge(0, 1, 9.0), new Edge(0, 1, 2.0),
                new Edge(1, 2, 3.0), new Edge(2, 0, 4.0));
        AlgorithmResult result = new KruskalAlgorithm().run(new GraphInput(nodes(3), edges, 0, null));

        assertThat(result.totalDistance()).isEqualTo(5.0);
    }

    @Test
    void kruskalOnSingleNodeOrNoEdges() {
        for (int count : List.of(1, 3)) {
            AlgorithmResult result = new KruskalAlgorithm().run(new GraphInput(nodes(count), List.of(), 0, null));
            assertThat(result.steps()).isEmpty();
            assertThat(result.totalDistance()).isEqualTo(0.0);
        }
    }

    @Test
    void kruskalAllowsNegativeWeights() {
        List<Edge> edges = List.of(new Edge(0, 1, -2.0), new Edge(1, 2, 3.0), new Edge(0, 2, 1.0));
        AlgorithmResult result = new KruskalAlgorithm().run(new GraphInput(nodes(3), edges, 0, null));

        assertThat(result.totalDistance()).isEqualTo(-1.0);
    }
}
