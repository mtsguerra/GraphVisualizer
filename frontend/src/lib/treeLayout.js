/**
 * Builds the forest formed by the edges currently marked on the graph and lays
 * it out in layers (y = depth), with parents centred above their children.
 *
 * @param edgeKeys  iterable of "from->to" keys (visual.edges.keys())
 * @param rootId    preferred root (the start node); other components get their own root
 * @returns {{ items: {id, x, y, parent}[], width: number, depth: number }}
 */
export function layoutTree(edgeKeys, rootId, nodeIds) {
  const adjacency = new Map()
  const link = (a, b) => {
    if (!adjacency.has(a)) adjacency.set(a, [])
    adjacency.get(a).push(b)
  }
  for (const key of edgeKeys) {
    const [from, to] = key.split('->').map(Number)
    link(from, to)
    link(to, from)
  }

  if (adjacency.size === 0) return { items: [], width: 1, depth: 0 }

  const known = new Set(nodeIds)
  const roots = []
  if (rootId != null && known.has(rootId)) roots.push(rootId)
  for (const id of adjacency.keys()) if (id !== rootId && known.has(id)) roots.push(id)

  const placed = new Set()
  const items = []
  let nextX = 0
  let depth = 0

  function place(id, parent, level) {
    placed.add(id)
    depth = Math.max(depth, level)
    const children = (adjacency.get(id) ?? []).filter((c) => known.has(c) && !placed.has(c))
    const childItems = []
    for (const child of children) {
      if (placed.has(child)) continue
      childItems.push(place(child, id, level + 1))
    }
    const item = {
      id,
      parent,
      y: level,
      x: childItems.length ? (childItems[0].x + childItems[childItems.length - 1].x) / 2 : nextX++,
    }
    items.push(item)
    return item
  }

  for (const root of roots) {
    if (!placed.has(root)) place(root, null, 0)
  }
  return { items, width: Math.max(nextX, 1), depth }
}
