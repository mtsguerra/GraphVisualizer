import { useEffect } from 'react'
import { COLORS } from '../lib/constants'
import { formatDistance } from '../lib/stepState'
import { layoutTree } from '../lib/treeLayout'

const COL_W = 80
const ROW_H = 90
const PAD = 40
const RADIUS = 22

const FILL = {
  exploring: [COLORS.unvisited, COLORS.exploring],
  current: [COLORS.current, COLORS.current],
  visited: [COLORS.visited, COLORS.visitedBorder],
  path: [COLORS.path, COLORS.path],
}

export default function TreeView({ nodes, startNode, visual, onClose }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  const { items, width, depth } = layoutTree(
    visual.edges.keys(),
    startNode,
    nodes.map((n) => n.id),
  )
  const labelOf = (id) => nodes.find((n) => n.id === id)?.label ?? id
  const pos = new Map(items.map((i) => [i.id, { x: PAD + i.x * COL_W, y: PAD + i.y * ROW_H }]))
  const w = PAD * 2 + (width - 1) * COL_W
  const h = PAD * 2 + depth * ROW_H

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" role="dialog" aria-label="Current tree" onClick={(e) => e.stopPropagation()}>
        <div className="modal-head">
          <h2>Current tree · {items.length} nodes</h2>
          <button type="button" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>
        {items.length === 0 ? (
          <p className="hint">No tree yet. Play or step through an algorithm to grow it.</p>
        ) : (
          <svg className="tree-svg" viewBox={`0 0 ${w} ${h}`} style={{ width: Math.max(w, 280), maxWidth: '100%' }}>
            {items
              .filter((i) => i.parent != null)
              .map((i) => {
                const a = pos.get(i.parent)
                const b = pos.get(i.id)
                return <line key={`e${i.id}`} x1={a.x} y1={a.y} x2={b.x} y2={b.y} stroke={COLORS.edgeLabel} strokeWidth={2} />
              })}
            {items.map((i) => {
              const p = pos.get(i.id)
              const [fill, stroke] = FILL[visual.nodes.get(i.id)] ?? FILL.exploring
              const d = visual.distances.get(i.id)
              return (
                <g key={i.id}>
                  <circle cx={p.x} cy={p.y} r={RADIUS} fill={fill} stroke={i.id === startNode ? COLORS.start : stroke} strokeWidth={i.id === startNode ? 4 : 2} />
                  <text x={p.x} y={p.y + 5} textAnchor="middle" fill={fill === COLORS.current || fill === COLORS.path ? COLORS.ink : COLORS.text} fontSize={14} fontWeight={600}>
                    {labelOf(i.id)}
                  </text>
                  {d != null && (
                    <text x={p.x} y={p.y + RADIUS + 14} textAnchor="middle" fill={COLORS.edgeLabel} fontSize={11}>
                      {formatDistance(d)}
                    </text>
                  )}
                </g>
              )
            })}
          </svg>
        )}
      </div>
    </div>
  )
}
