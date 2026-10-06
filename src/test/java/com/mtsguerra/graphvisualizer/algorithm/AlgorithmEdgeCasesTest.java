package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.Edge;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.Node;
import com.mtsguerra.graphvisualizer.model.Step;
import com.mtsguerra.graphvisualizer.model.StepAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Edge cases shared by every algorithm: cycles, disconnected graphs, degenerate sizes, weights. */
class AlgorithmEdgeCasesTest {

    private static List<Node> nodes(int count) {
        return IntStream.range(0, count).mapToObj(i -> new Node(i, "N" + i, i * 10, 0)).toList();
    }

    private static List<GraphAlgorithm> allAlgorithms() {
        return List.of(new BFSAlgorithm(), new DFSAlgorithm(), new DijkstraAlgorithm(), new AStar());
    }

    private static List<GraphAlgorithm> weightedAlgorithms() {
        return List.of(new DijkstraAlgorithm(), new AStar());
    }

    @Test
    void singleNodeGraph() {
        GraphInput input = new GraphInput(nodes(1), List.of(), 0, 0);

        for (GraphAlgorithm algorithm : allAlgorithms()) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.path()).as(result.algorithm()).containsExactly(0);
            assertThat(result.steps()).as(result.algorithm()).isNotEmpty();
        }
    }

    @Test
    void singleNodeWithoutEndForTraversals() {
        GraphInput input = new GraphInput(nodes(1), List.of(), 0, null);

        for (GraphAlgorithm algorithm : List.of(new BFSAlgorithm(), new DFSAlgorithm())) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.path()).isNull();
            assertThat(result.steps()).extracting(Step::nodeId).containsOnly(0);
        }
    }

    @Test
    void cycleTerminatesAndVisitsEachNodeOnce() {
        List<Edge> ring = List.of(new Edge(0, 1), new Edge(1, 2), new Edge(2, 3), new Edge(3, 0));
        GraphInput input = new GraphInput(nodes(4), ring, 0, null);

        for (GraphAlgorithm algorithm : List.of(new BFSAlgorithm(), new DFSAlgorithm())) {
            AlgorithmResult result = algorithm.run(input);
            List<Integer> visited = result.steps().stream()
                    .filter(s -> s.action() == StepAction.VISITED).map(Step::nodeId).toList();
            assertThat(visited).as(result.algorithm()).doesNotHaveDuplicates().hasSize(4);
        }
    }

    @Test
    void cycleDoesNotInflateShortestPath() {
        // 0-1-2 with a back edge 2-0 that is expensive; direct 0-1-2 stays cheapest.
        List<Edge> edges = List.of(new Edge(0, 1, 1.0), new Edge(1, 2, 1.0), new Edge(2, 0, 10.0));
        GraphInput input = new GraphInput(nodes(3), edges, 0, 2);

        for (GraphAlgorithm algorithm : weightedAlgorithms()) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.path()).as(result.algorithm()).containsExactly(0, 1, 2);
            assertThat(result.totalDistance()).as(result.algorithm()).isEqualTo(2.0);
        }
    }

    @Test
    void selfLoopIsIgnored() {
        List<Edge> edges = List.of(new Edge(0, 0, 1.0), new Edge(0, 1, 5.0));
        GraphInput input = new GraphInput(nodes(2), edges, 0, 1);

        for (GraphAlgorithm algorithm : allAlgorithms()) {
            assertThat(algorithm.run(input).path()).as(algorithm.getClass().getSimpleName()).containsExactly(0, 1);
        }
    }

    @Test
    void disconnectedComponentIsNeverReached() {
        // Component {0,1} and component {2,3}.
        GraphInput input = new GraphInput(nodes(4), List.of(new Edge(0, 1), new Edge(2, 3)), 0, null);

        AlgorithmResult bfs = new BFSAlgorithm().run(input);
        assertThat(bfs.steps()).extracting(Step::nodeId).doesNotContain(2, 3);

        AlgorithmResult dfs = new DFSAlgorithm().run(input);
        assertThat(dfs.steps()).extracting(Step::nodeId).doesNotContain(2, 3);
    }

    @Test
    void unreachableTargetInOtherComponentGivesEmptyPath() {
        GraphInput input = new GraphInput(nodes(4), List.of(new Edge(0, 1), new Edge(2, 3)), 0, 3);

        for (GraphAlgorithm algorithm : allAlgorithms()) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.path()).as(result.algorithm()).isEmpty();
            assertThat(result.totalDistance()).as(result.algorithm()).isNull();
            assertThat(result.steps()).as(result.algorithm())
                    .noneMatch(s -> s.action() == StepAction.PATH);
        }
    }

    @Test
    void noEdgesAtAll() {
        GraphInput input = new GraphInput(nodes(3), List.of(), 0, 2);

        for (GraphAlgorithm algorithm : allAlgorithms()) {
            assertThat(algorithm.run(input).path()).as(algorithm.getClass().getSimpleName()).isEmpty();
        }
    }

    @Test
    void negativeWeightRejectedEvenWhenEdgeIsUnreachable() {
        List<Edge> edges = List.of(new Edge(0, 1, 1.0), new Edge(2, 3, -4.0));
        GraphInput input = new GraphInput(nodes(4), edges, 0, 1);

        for (GraphAlgorithm algorithm : weightedAlgorithms()) {
            assertThatThrownBy(() -> algorithm.run(input))
                    .isInstanceOf(InvalidGraphException.class).hasMessageContaining("Negative weight");
        }
    }

    @Test
    void negativeWeightIgnoredByUnweightedTraversals() {
        GraphInput input = new GraphInput(nodes(2), List.of(new Edge(0, 1, -1.0)), 0, 1);

        assertThat(new BFSAlgorithm().run(input).path()).containsExactly(0, 1);
        assertThat(new DFSAlgorithm().run(input).path()).containsExactly(0, 1);
    }

    @Test
    void zeroWeightEdgesAreAllowed() {
        List<Edge> edges = List.of(new Edge(0, 1, 0.0), new Edge(1, 2, 0.0), new Edge(0, 2, 1.0));
        GraphInput input = new GraphInput(nodes(3), edges, 0, 2);

        AlgorithmResult result = new DijkstraAlgorithm().run(input);
        assertThat(result.path()).containsExactly(0, 1, 2);
        assertThat(result.totalDistance()).isEqualTo(0.0);
    }

    @Test
    void parallelEdgesUseTheCheapest() {
        List<Edge> edges = List.of(new Edge(0, 1, 9.0), new Edge(0, 1, 2.0), new Edge(0, 1, 5.0));
        GraphInput input = new GraphInput(nodes(2), edges, 0, 1);

        for (GraphAlgorithm algorithm : weightedAlgorithms()) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.totalDistance()).as(result.algorithm()).isEqualTo(2.0);
        }
    }

    @Test
    void dijkstraPrefersCheaperPathOverFewerEdges() {
        // Direct 0->3 costs 10; 0->1->2->3 costs 3. BFS takes the direct edge, Dijkstra must not.
        List<Edge> edges = List.of(new Edge(0, 3, 10.0), new Edge(0, 1, 1.0), new Edge(1, 2, 1.0), new Edge(2, 3, 1.0));
        GraphInput input = new GraphInput(nodes(4), edges, 0, 3);

        assertThat(new BFSAlgorithm().run(input).path()).containsExactly(0, 3);
        AlgorithmResult dijkstra = new DijkstraAlgorithm().run(input);
        assertThat(dijkstra.path()).containsExactly(0, 1, 2, 3);
        assertThat(dijkstra.totalDistance()).isEqualTo(3.0);
    }

    @Test
    void largeChainRunsWithoutStackOverflowOrQuadraticBlowup() {
        int n = 2_000;
        List<Edge> chain = IntStream.range(0, n - 1).mapToObj(i -> new Edge(i, i + 1, 1.0)).toList();
        GraphInput input = new GraphInput(nodes(n), chain, 0, n - 1);

        for (GraphAlgorithm algorithm : List.of(new BFSAlgorithm(), new DijkstraAlgorithm(), new AStar())) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.path()).as(result.algorithm()).hasSize(n);
        }
    }
}
