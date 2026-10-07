export function edgeKey(from, to) {
  return `${from}->${to}`
}

/**
 * Replays the first `count` steps and returns what the graph should look like.
 *
 * - nodes:     nodeId -> 'exploring' | 'current' | 'visited' | 'path'
 * - edges:     edgeKey -> 'tree' | 'path'. 'tree' is the edge each node was most
 *              recently reached through, so for Dijkstra/A* it shows the current
 *              shortest-path tree rather than every edge ever relaxed.
 * - distances: nodeId -> best known distance (weighted algorithms only)
 */
export function computeVisualState(steps, count) {
  const nodes = new Map()
  const distances = new Map()
  const treeEdgeByNode = new Map()
  const pathEdges = new Set()
  // Nodes the algorithm has actually processed. DFS only marks a node 'visited'
  // when it backtracks, so 'current' counts too.
  const visited = new Set()
  let queuePops = 0
  let relaxedEdges = 0

  for (let i = 0; i < count; i++) {
    const step = steps[i]
    nodes.set(step.nodeId, step.action)
    if (step.distance != null) distances.set(step.nodeId, step.distance)
    if (step.action === 'current') queuePops++
    if (step.action === 'current' || step.action === 'visited') visited.add(step.nodeId)
    if (step.edge && step.action !== 'path') relaxedEdges++
    if (step.edge) {
      const key = edgeKey(step.edge.from, step.edge.to)
      if (step.action === 'path') pathEdges.add(key)
      else treeEdgeByNode.set(step.nodeId, key)
    }
  }

  const edges = new Map()
  for (const key of treeEdgeByNode.values()) edges.set(key, 'tree')
  for (const key of pathEdges) edges.set(key, 'path')

  return {
    nodes,
    edges,
    distances,
    visitedCount: visited.size,
    queuePops,
    relaxedEdges,
    lastStep: count > 0 ? steps[count - 1] : null,
  }
}

/** State of a graph edge; undirected edges match steps traversed either way. */
export function edgeStateFor(edge, directed, visual) {
  return (
    visual.edges.get(edgeKey(edge.from, edge.to)) ??
    (directed ? undefined : visual.edges.get(edgeKey(edge.to, edge.from)))
  )
}

export function formatDistance(distance) {
  return Number.isInteger(distance) ? String(distance) : distance.toFixed(2)
}
