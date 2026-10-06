package com.mtsguerra.graphvisualizer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Response body for every algorithm endpoint.
 *
 * @param path          node ids from start to end; null when no endNode was given,
 *                      empty when endNode is unreachable
 * @param totalDistance sum of edge weights along the path (weighted algorithms only)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AlgorithmResult(String algorithm, List<Step> steps, List<Integer> path, Double totalDistance) {
}
