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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlgorithmsTest {

    private final GraphInput sample = SampleGraphs.weighted();

    @Test
    void bfsFindsPathWithFewestEdges() {
        AlgorithmResult result = new BFSAlgorithm().run(sample);

        assertThat(result.path()).containsExactly(0, 1, 3, 5);
        assertThat(result.totalDistance()).isNull();
        assertThat(result.steps().getFirst()).isEqualTo(new Step(0, StepAction.EXPLORING, 0, null, null));
        assertTimestampsSequential(result);
    }

    @Test
    void bfsWithoutEndNodeVisitsWholeComponent() {
        GraphInput input = new GraphInput(sample.nodes(), sample.edges(), 0, null);

        AlgorithmResult result = new BFSAlgorithm().run(input);

        assertThat(result.path()).isNull();
        assertThat(nodesWith(result, StepAction.VISITED)).containsExactly(0, 1, 2, 3, 4, 5);
    }

    @Test
    void dfsGoesDeepBeforeWide() {
        AlgorithmResult result = new DFSAlgorithm().run(sample);

        // A's first neighbor is B, B's first unseen is C, C's is E, E's is D, D's is F.
        assertThat(result.path()).containsExactly(0, 1, 2, 4, 3, 5);
        assertTimestampsSequential(result);
    }

    @Test
    void dfsMarksEveryNodeVisitedWhenNoEndNode() {
        GraphInput input = new GraphInput(sample.nodes(), sample.edges(), 0, null);

        AlgorithmResult result = new DFSAlgorithm().run(input);

        assertThat(Set.copyOf(nodesWith(result, StepAction.VISITED))).containsExactlyInAnyOrder(0, 1, 2, 3, 4, 5);
        // The start node finishes last, after backtracking out of everything else.
        assertThat(result.steps().getLast()).extracting(Step::nodeId, Step::action)
                .containsExactly(0, StepAction.VISITED);
    }

    @Test
    void dijkstraFindsCheapestPath() {
        AlgorithmResult result = new DijkstraAlgorithm().run(sample);

        assertThat(result.path()).containsExactly(0, 2, 1, 3, 4, 5);
        assertThat(result.totalDistance()).isEqualTo(13.0);
        List<Step> pathSteps = result.steps().stream().filter(s -> s.action() == StepAction.PATH).toList();
        assertThat(pathSteps).extracting(Step::distance).containsExactly(0.0, 2.0, 3.0, 8.0, 10.0, 13.0);
        assertThat(pathSteps.get(1).edge()).isEqualTo(new Edge(0, 2, 2.0));
        assertTimestampsSequential(result);
    }

    @Test
    void aStarMatchesDijkstraAndExploresNoMore() {
        AlgorithmResult aStar = new AStar().run(sample);
        AlgorithmResult dijkstra = new DijkstraAlgorithm().run(sample);

        assertThat(aStar.path()).isEqualTo(dijkstra.path());
        assertThat(aStar.totalDistance()).isEqualTo(13.0);
        assertThat(nodesWith(aStar, StepAction.VISITED).size())
                .isLessThanOrEqualTo(nodesWith(dijkstra, StepAction.VISITED).size());
    }

    @Test
    void aStarPrefersNodesTowardTheGoal() {
        // Straight line 0 -> 1 -> 2 -> 3 (goal), plus a decoy 0 -> 4 going the other way.
        List<Node> nodes = List.of(
                new Node(0, "S", 0, 0), new Node(1, "a", 10, 0), new Node(2, "b", 20, 0),
                new Node(3, "G", 30, 0), new Node(4, "x", -10, 0));
        List<Edge> edges = List.of(
                new Edge(0, 4, 10.0), new Edge(0, 1, 10.0), new Edge(1, 2, 10.0), new Edge(2, 3, 10.0));
        GraphInput input = new GraphInput(nodes, edges, 0, 3);

        AlgorithmResult result = new AStar().run(input);

        assertThat(result.path()).containsExactly(0, 1, 2, 3);
        assertThat(nodesWith(result, StepAction.CURRENT)).doesNotContain(4);
    }

    @Test
    void unreachableEndGivesEmptyPath() {
        List<Node> nodes = List.of(new Node(0, "A", 0, 0), new Node(1, "B", 1, 0), new Node(2, "C", 2, 0));
        GraphInput input = new GraphInput(nodes, List.of(new Edge(0, 1)), 0, 2);

        for (GraphAlgorithm algorithm : allAlgorithms()) {
            AlgorithmResult result = algorithm.run(input);
            assertThat(result.path()).as(result.algorithm()).isEmpty();
            assertThat(result.totalDistance()).as(result.algorithm()).isNull();
        }
    }

    @Test
    void startEqualsEndGivesSingleNodePath() {
        GraphInput input = new GraphInput(sample.nodes(), sample.edges(), 3, 3);

        for (GraphAlgorithm algorithm : allAlgorithms()) {
            assertThat(algorithm.run(input).path()).as(algorithm.getClass().getSimpleName()).containsExactly(3);
        }
    }

    @Test
    void directedGraphIgnoresReverseEdges() {
        List<Node> nodes = List.of(new Node(0, "A", 0, 0), new Node(1, "B", 1, 0));
        GraphInput input = new GraphInput(nodes, List.of(new Edge(1, 0)), 0, 1, true);

        assertThat(new BFSAlgorithm().run(input).path()).isEmpty();
    }

    @Test
    void rejectsInvalidInput() {
        GraphInput unknownStart = new GraphInput(sample.nodes(), sample.edges(), 99, 5);
        assertThatThrownBy(() -> new BFSAlgorithm().run(unknownStart))
                .isInstanceOf(InvalidGraphException.class).hasMessageContaining("startNode");

        GraphInput missingEnd = new GraphInput(sample.nodes(), sample.edges(), 0, null);
        assertThatThrownBy(() -> new DijkstraAlgorithm().run(missingEnd))
                .isInstanceOf(InvalidGraphException.class).hasMessageContaining("endNode is required");

        GraphInput badEdge = new GraphInput(sample.nodes(), List.of(new Edge(0, 42)), 0, 5);
        assertThatThrownBy(() -> new DFSAlgorithm().run(badEdge))
                .isInstanceOf(InvalidGraphException.class).hasMessageContaining("42");

        GraphInput negative = new GraphInput(sample.nodes(), List.of(new Edge(0, 5, -1.0)), 0, 5);
        assertThatThrownBy(() -> new AStar().run(negative))
                .isInstanceOf(InvalidGraphException.class).hasMessageContaining("Negative weight");

        List<Node> duplicated = List.of(new Node(0, "A", 0, 0), new Node(0, "B", 0, 0));
        assertThatThrownBy(() -> new BFSAlgorithm().run(new GraphInput(duplicated, List.of(), 0, null)))
                .isInstanceOf(InvalidGraphException.class).hasMessageContaining("Duplicate");
    }

    private static List<GraphAlgorithm> allAlgorithms() {
        return List.of(new BFSAlgorithm(), new DFSAlgorithm(), new DijkstraAlgorithm(), new AStar());
    }

    private static List<Integer> nodesWith(AlgorithmResult result, StepAction action) {
        return result.steps().stream().filter(s -> s.action() == action).map(Step::nodeId)
                .collect(Collectors.toList());
    }

    private static void assertTimestampsSequential(AlgorithmResult result) {
        assertThat(result.steps()).extracting(Step::timestamp)
                .containsExactlyElementsOf(IntStream.range(0, result.steps().size()).boxed().toList());
    }
}
