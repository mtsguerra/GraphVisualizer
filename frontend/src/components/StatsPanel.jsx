import { COLORS } from '../lib/constants'
import { formatDistance } from '../lib/stepState'

const LEGEND = [
  ['Unvisited', COLORS.unvisited, COLORS.unvisitedBorder],
  ['Frontier', COLORS.unvisited, COLORS.exploring],
  ['Evaluating', COLORS.current, COLORS.current],
  ['Visited', COLORS.visited, COLORS.visitedBorder],
  ['Final path', COLORS.path, COLORS.path],
  ['Start', COLORS.start, COLORS.start],
  ['End', COLORS.end, COLORS.end],
]

const ACTION_TEXT = {
  exploring: 'is discovered',
  current: 'is being processed',
  visited: 'is done',
  path: 'is on the final path',
}

function describeStep(step, labelOf) {
  if (!step) return 'Press Play to run the algorithm.'
  const via = step.edge ? ` via ${labelOf(step.edge.from)} → ${labelOf(step.edge.to)}` : ''
  const distance = step.distance != null ? ` (distance ${formatDistance(step.distance)})` : ''
  return `${labelOf(step.nodeId)} ${ACTION_TEXT[step.action] ?? step.action}${via}${distance}`
}

function costText(result, finished) {
  if (!result || result.path == null) return '—'
  if (!finished) return '…'
  if (result.path.length === 0) return 'No path'
  return result.totalDistance != null ? formatDistance(result.totalDistance) : `${result.path.length - 1} edges`
}

function Card({ label, value, sub, tone }) {
  return (
    <div className={`stat-card ${tone ?? ''}`}>
      <dt>{label}</dt>
      <dd>{value}</dd>
      {sub && <small>{sub}</small>}
    </div>
  )
}

export default function StatsPanel({ result, visual, stepIndex, totalNodes, requestMs, nodes }) {
  const labelOf = (id) => nodes.find((n) => n.id === id)?.label ?? id
  const total = result?.steps.length ?? 0
  const finished = result != null && stepIndex >= total
  const percent = totalNodes > 0 ? Math.round((visual.visitedCount / totalNodes) * 100) : 0
  const fmt = (n) => (result ? n.toLocaleString() : '—')

  return (
    <section className="panel">
      <h2>Statistics</h2>
      <dl className="stat-grid">
        <Card label="Path cost" value={costText(result, finished)} tone="violet" />
        <Card label="Step" value={result ? `${stepIndex} / ${total}` : '—'} />
        <Card label="Queue pops" value={fmt(visual.queuePops)} />
        <Card label="Edges relaxed" value={fmt(visual.relaxedEdges)} />
        <Card
          label="Run time"
          value={requestMs != null ? `${requestMs.toFixed(0)} ms` : '—'}
          sub={result ? `API round-trip · ${total} anim. steps` : undefined}
        />
        <Card label="Nodes visited" value={result ? `${visual.visitedCount} / ${totalNodes}` : '—'} sub={result ? `${percent}%` : undefined} />
      </dl>
      <div
        className="progress"
        role="progressbar"
        aria-label="Nodes visited"
        aria-valuemin={0}
        aria-valuemax={totalNodes}
        aria-valuenow={visual.visitedCount}
      >
        <div style={{ width: `${result ? percent : 0}%` }} />
      </div>
      {finished && result.path?.length > 0 && <p className="path-line">{result.path.map(labelOf).join(' → ')}</p>}
      <p className="step-line" aria-live="polite">
        {describeStep(visual.lastStep, labelOf)}
      </p>
      <ul className="legend">
        {LEGEND.map(([label, fill, border]) => (
          <li key={label}>
            <span className="swatch" style={{ background: fill, borderColor: border }} />
            {label}
          </li>
        ))}
      </ul>
    </section>
  )
}
