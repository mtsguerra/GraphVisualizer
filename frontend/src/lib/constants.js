export const ALGORITHMS = [
  { id: 'bfs', name: 'Breadth-First Search', short: 'BFS', needsEnd: false },
  { id: 'dfs', name: 'Depth-First Search', short: 'DFS', needsEnd: false },
  { id: 'dijkstra', name: "Dijkstra's Shortest Path", short: 'Dijkstra', needsEnd: true },
  { id: 'astar', name: 'A* Search', short: 'A*', needsEnd: true },
  { id: 'widest', name: 'Widest Path', short: 'Widest', needsEnd: true },
  { id: 'kruskal', name: "Kruskal's Spanning Tree", short: 'Kruskal', needsEnd: false },
]

export const COLORS = {
  unvisited: '#334155',
  unvisitedBorder: '#64748b',
  exploring: '#38bdf8',
  current: '#fbbf24',
  visited: '#115e59',
  visitedBorder: '#2dd4bf',
  path: '#a78bfa',
  start: '#10b981',
  end: '#f43f5e',
  edge: '#475569',
  edgeLabel: '#94a3b8',
  canvas: '#0b1220',
  ink: '#0f172a',
  text: '#e2e8f0',
}

/** Playback speed multiplier; 1x advances one step every BASE_STEP_MS. */
export const MIN_SPEED = 0.25
export const MAX_SPEED = 5
export const SPEED_STEP = 0.25
export const DEFAULT_SPEED = 1
export const BASE_STEP_MS = 500
