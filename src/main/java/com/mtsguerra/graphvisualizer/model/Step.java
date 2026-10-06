package com.mtsguerra.graphvisualizer.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One frame of the animation.
 *
 * @param timestamp position of this step in the sequence (0, 1, 2, ...)
 * @param edge      edge that led to this node, oriented in the direction it was
 *                  traversed; null when there isn't one (e.g. the start node)
 * @param distance  best known distance from the start node (weighted algorithms only)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Step(int nodeId, StepAction action, int timestamp, Edge edge, Double distance) {
}
