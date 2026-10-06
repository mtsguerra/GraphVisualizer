package com.mtsguerra.graphvisualizer.algorithm;

import com.mtsguerra.graphvisualizer.model.AlgorithmResult;
import com.mtsguerra.graphvisualizer.model.GraphInput;

public interface GraphAlgorithm {

    AlgorithmResult run(GraphInput input);
}
