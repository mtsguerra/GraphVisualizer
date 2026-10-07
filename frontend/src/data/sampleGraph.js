// Same graph as the backend's SampleGraphs.weighted(), so the app works
// even before the backend is up. Start A (0), end F (5).
//   Fewest edges (BFS):     A-B-D-F       cost 15
//   Cheapest (Dijkstra/A*): A-C-B-D-E-F   cost 13
export const SAMPLE_GRAPH = {
  nodes: [
    { id: 0, label: 'A', x: 100, y: 200 },
    { id: 1, label: 'B', x: 250, y: 100 },
    { id: 2, label: 'C', x: 250, y: 300 },
    { id: 3, label: 'D', x: 450, y: 100 },
    { id: 4, label: 'E', x: 450, y: 300 },
    { id: 5, label: 'F', x: 600, y: 200 },
  ],
  edges: [
    { from: 0, to: 1, weight: 4 },
    { from: 0, to: 2, weight: 2 },
    { from: 1, to: 2, weight: 1 },
    { from: 1, to: 3, weight: 5 },
    { from: 2, to: 4, weight: 10 },
    { from: 2, to: 3, weight: 8 },
    { from: 3, to: 4, weight: 2 },
    { from: 3, to: 5, weight: 6 },
    { from: 4, to: 5, weight: 3 },
  ],
  directed: false,
}

export const SAMPLE_START = 0
export const SAMPLE_END = 5
