package com.mtsguerra.graphvisualizer.controller;

import com.mtsguerra.graphvisualizer.algorithm.AStar;
import com.mtsguerra.graphvisualizer.algorithm.BFSAlgorithm;
import com.mtsguerra.graphvisualizer.algorithm.DFSAlgorithm;
import com.mtsguerra.graphvisualizer.algorithm.DijkstraAlgorithm;
import com.mtsguerra.graphvisualizer.algorithm.KruskalAlgorithm;
import com.mtsguerra.graphvisualizer.algorithm.WidestPathAlgorithm;
import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.GraphInput;
import com.mtsguerra.graphvisualizer.model.SampleGraphs;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GraphController {

    private final BFSAlgorithm bfs;
    private final DFSAlgorithm dfs;
    private final DijkstraAlgorithm dijkstra;
    private final AStar aStar;
    private final WidestPathAlgorithm widest;
    private final KruskalAlgorithm kruskal;

    public GraphController(BFSAlgorithm bfs, DFSAlgorithm dfs, DijkstraAlgorithm dijkstra, AStar aStar,
                           WidestPathAlgorithm widest, KruskalAlgorithm kruskal) {
        this.bfs = bfs;
        this.dfs = dfs;
        this.dijkstra = dijkstra;
        this.aStar = aStar;
        this.widest = widest;
        this.kruskal = kruskal;
    }

    @PostMapping("/bfs")
    public AlgorithmResult bfs(@RequestBody GraphInput input) {
        return bfs.run(input);
    }

    @PostMapping("/dfs")
    public AlgorithmResult dfs(@RequestBody GraphInput input) {
        return dfs.run(input);
    }

    @PostMapping("/dijkstra")
    public AlgorithmResult dijkstra(@RequestBody GraphInput input) {
        return dijkstra.run(input);
    }

    @PostMapping("/astar")
    public AlgorithmResult aStar(@RequestBody GraphInput input) {
        return aStar.run(input);
    }

    @PostMapping("/widest")
    public AlgorithmResult widest(@RequestBody GraphInput input) {
        return widest.run(input);
    }

    @PostMapping("/kruskal")
    public AlgorithmResult kruskal(@RequestBody GraphInput input) {
        return kruskal.run(input);
    }

    /** Sample graph the frontend can load and POST straight back to any endpoint. */
    @GetMapping("/sample")
    public GraphInput sample() {
        return SampleGraphs.weighted();
    }
}
