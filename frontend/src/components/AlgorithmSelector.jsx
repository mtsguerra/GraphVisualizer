import { ALGORITHMS } from '../lib/constants'

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
  disabled,
}) {
  const selected = ALGORITHMS.find((a) => a.id === algorithm)
  const parseNode = (value) => (value === '' ? null : Number(value))
  const togglePick = (kind) => onPickModeChange(pickMode === kind ? null : kind)

  return (
    <section className="panel">
      <h2>Algorithm</h2>
      <div className="chips" role="radiogroup" aria-label="Algorithm">
        {ALGORITHMS.map((a) => (
          <button
            key={a.id}
            type="button"
            role="radio"
            aria-checked={algorithm === a.id}
            title={a.name}
            className={algorithm === a.id ? 'active' : ''}
            onClick={() => onAlgorithmChange(a.id)}
            disabled={disabled}
          >
            {a.short}
          </button>
        ))}
      </div>
      <p className="hint">{selected.name}</p>

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
          Set start
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
          Set end
        </button>
      </div>
      {pickMode && <p className="hint accent">Click a node on the canvas to set the {pickMode}. Esc to cancel.</p>}
    </section>
  )
}
