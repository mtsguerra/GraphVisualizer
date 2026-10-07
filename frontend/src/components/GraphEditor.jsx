const MODES = [
  { id: 'move', label: 'Move', hint: 'Drag nodes to rearrange the graph.' },
  { id: 'addNode', label: 'Add node', hint: 'Click empty space to add a node.' },
  { id: 'addEdge', label: 'Add edge', hint: 'Click two nodes to connect them.' },
  { id: 'delete', label: 'Delete', hint: 'Click a node or edge to remove it.' },
]

export default function GraphEditor({
  mode,
  onModeChange,
  edgeWeight,
  onEdgeWeightChange,
  directed,
  onDirectedChange,
  onLoadSample,
  onClear,
  disabled,
}) {
  return (
    <section className="panel compact">
      <h2>Edit graph</h2>
      <div className="segmented" role="radiogroup" aria-label="Edit mode">
        {MODES.map((m) => (
          <button
            key={m.id}
            type="button"
            role="radio"
            aria-checked={mode === m.id}
            className={mode === m.id ? 'active' : ''}
            onClick={() => onModeChange(m.id)}
            disabled={disabled}
          >
            {m.label}
          </button>
        ))}
      </div>
      <p className="hint">{MODES.find((m) => m.id === mode).hint}</p>

      <div className="edit-row">
        <label className="field">
          <span>Edge weight</span>
          <input
            type="number"
            min={0}
            step="any"
            value={edgeWeight}
            onChange={(e) => onEdgeWeightChange(e.target.value)}
            disabled={disabled}
          />
        </label>
        <label className="checkbox">
          <input
            type="checkbox"
            checked={directed}
            onChange={(e) => onDirectedChange(e.target.checked)}
            disabled={disabled}
          />
          Directed
        </label>
        <div className="button-row">
          <button type="button" onClick={onLoadSample} disabled={disabled}>
            Load sample
          </button>
          <button type="button" onClick={onClear} disabled={disabled}>
            Clear
          </button>
        </div>
      </div>
    </section>
  )
}
