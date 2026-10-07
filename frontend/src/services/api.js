const API_BASE = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api').replace(/\/$/, '')

async function request(path, options) {
  let response
  try {
    response = await fetch(`${API_BASE}${path}`, options)
  } catch {
    throw new Error(`Can't reach the backend at ${API_BASE}. Is it running? (./gradlew bootRun)`)
  }
  if (!response.ok) {
    // The backend answers 400s with a ProblemDetail: { title, status, detail }.
    const body = await response.json().catch(() => null)
    throw new Error(body?.detail ?? `Request failed with status ${response.status}`)
  }
  return response.json()
}

/** @returns {Promise<{nodes, edges, startNode, endNode, directed}>} */
export function fetchSampleGraph() {
  return request('/sample')
}

/**
 * @param {'bfs'|'dfs'|'dijkstra'|'astar'|'widest'|'kruskal'} algorithm
 * @returns {Promise<{algorithm, steps, path?, totalDistance?}>}
 */
export function runAlgorithm(algorithm, { graph, startNode, endNode }) {
  return request(`/${algorithm}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      nodes: graph.nodes,
      edges: graph.edges.map(({ from, to, weight }) => ({ from, to, weight })),
      startNode,
      endNode,
      directed: graph.directed,
    }),
  })
}
