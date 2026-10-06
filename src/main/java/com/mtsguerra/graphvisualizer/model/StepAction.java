package com.mtsguerra.graphvisualizer.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StepAction {
    /** Node was discovered and added to the frontier (queue / priority queue). */
    EXPLORING,
    /** Node is the one being processed right now. */
    CURRENT,
    /** Node is fully processed. */
    VISITED,
    /** Node is part of the final path from start to end. */
    PATH;

    @JsonValue
    public String json() {
        return name().toLowerCase();
    }
}
