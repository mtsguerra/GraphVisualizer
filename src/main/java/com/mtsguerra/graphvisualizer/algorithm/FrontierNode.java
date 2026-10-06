package com.mtsguerra.graphvisualizer.algorithm;

/**
 * Priority-queue entry: a node paired with the cost it was queued at. A node can
 * sit in the queue several times; the entry whose cost no longer matches the best
 * known one is stale and skipped when polled (lazy deletion).
 */
record FrontierNode(int node, double cost) {
}
