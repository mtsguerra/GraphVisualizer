package com.mtsguerra.graphvisualizer.model;

/**
 * A graph vertex. The x/y coordinates are canvas positions used by the
 * frontend for drawing and by A* for its distance heuristic.
 */
public record Node(int id, String label, double x, double y) {
}
