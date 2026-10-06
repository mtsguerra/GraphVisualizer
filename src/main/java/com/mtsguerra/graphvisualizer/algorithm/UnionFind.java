package com.mtsguerra.graphvisualizer.algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** Disjoint sets over arbitrary node ids, with path compression and union by rank. */
final class UnionFind {

    private final Map<Integer, Integer> parent = new HashMap<>();
    private final Map<Integer, Integer> rank = new HashMap<>();

    UnionFind(Collection<Integer> ids) {
        for (int id : ids) {
            parent.put(id, id);
            rank.put(id, 0);
        }
    }

    int find(int x) {
        int root = x;
        while (parent.get(root) != root) {
            root = parent.get(root);
        }
        while (parent.get(x) != root) {
            int next = parent.get(x);
            parent.put(x, root);
            x = next;
        }
        return root;
    }

    /** @return false if x and y were already in the same set */
    boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false;
        }
        int rankX = rank.get(rootX);
        int rankY = rank.get(rootY);
        if (rankX < rankY) {
            parent.put(rootX, rootY);
        } else if (rankY < rankX) {
            parent.put(rootY, rootX);
        } else {
            parent.put(rootY, rootX);
            rank.put(rootX, rankX + 1);
        }
        return true;
    }
}
