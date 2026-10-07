import { ALGORITHMS, COLORS } from '../lib/constants'

const LEGEND = [
  ['Unvisited', COLORS.unvisited, COLORS.unvisitedBorder],
  ['Frontier', COLORS.unvisited, COLORS.exploring],
  ['Evaluating', COLORS.current, COLORS.current],
  ['Visited', COLORS.visited, COLORS.visitedBorder],
  ['Final path', COLORS.path, COLORS.path],
  ['Start', COLORS.start, COLORS.start],
  ['End', COLORS.end, COLORS.end],
]

export default function AlgorithmSelector({
  algorithm,
  onAlgorithmChange,
  nodes,
  startNode,
  endNode,
  onStartChange,
  onEndChange,
  pickMode,
  onPickModeChange,
  pathText,
  disabled,
}) {
  const selected = ALGORITHMS.find((a) => a.id === algorithm)
  const parseNode = (value) => (value === '' ? null : Number(value))
  const togglePick = (kind) => onPickModeChange(pickMode === kind ? null : kind)

  return (
    <section className="panel compact">
      <h2>Algorithm</h2>
      <select aria-label="Algorithm" title={selected.name} value={algorithm} onChange={(e) => onAlgorithmChange(e.target.value)} disabled={disabled}>
        {ALGORITHMS.map((a) => (
          <option key={a.id} value={a.id}>
            {a.name}
          </option>
        ))}
      </select>

      <div className="endpoints">
      <div className="endpoint start">
        <span className="marker" aria-hidden="true">▶</span>
        <select
          aria-label="Start node"
          value={startNode ?? ''}
          onChange={(e) => onStartChange(parseNode(e.target.value))}
          disabled={disabled}
        >
          {startNode == null && <option value="">—</option>}
          {nodes.map((n) => (
            <option key={n.id} value={n.id}>
              {n.label}
            </option>
          ))}
        </select>
        <button
          type="button"
          className={pickMode === 'start' ? 'pick active' : 'pick'}
          aria-pressed={pickMode === 'start'}
          onClick={() => togglePick('start')}
          disabled={disabled}
        >
          Set
        </button>
      </div>

      <div className="endpoint end">
        <span className="marker" aria-hidden="true">◎</span>
        <select aria-label="End node" value={endNode ?? ''} onChange={(e) => onEndChange(parseNode(e.target.value))} disabled={disabled}>
          <option value="">{selected.needsEnd ? '— required —' : 'None (explore all)'}</option>
          {nodes.map((n) => (
            <option key={n.id} value={n.id}>
              {n.label}
            </option>
          ))}
        </select>
        <button
          type="button"
          className={pickMode === 'end' ? 'pick active' : 'pick'}
          aria-pressed={pickMode === 'end'}
          onClick={() => togglePick('end')}
          disabled={disabled}
        >
          Set
        </button>
      </div>
      </div>
      {pickMode && <p className="hint accent">Click a node on the canvas to set the {pickMode}. Esc to cancel.</p>}
      {pathText && <p className="path-line">{pathText}</p>}
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
