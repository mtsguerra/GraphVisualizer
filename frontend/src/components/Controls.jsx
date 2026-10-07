import { MAX_SPEED, MIN_SPEED, SPEED_STEP } from '../lib/constants'

export default function Controls({
  playing,
  loading,
  canPlay,
  stepIndex,
  totalSteps,
  speed,
  onPlay,
  onPause,
  onStepBack,
  onStepForward,
  onReset,
  onSeek,
  onSpeedChange,
}) {
  const hasSteps = totalSteps > 0

  return (
    <section className="panel compact">
      <h2>Playback</h2>
      <div className="button-row">
        {playing ? (
          <button type="button" className="primary" onClick={onPause}>
            ⏸ Pause
          </button>
        ) : (
          <button type="button" className="primary" onClick={onPlay} disabled={!canPlay || loading}>
            {loading ? 'Running…' : stepIndex > 0 && stepIndex < totalSteps ? '▶ Resume' : '▶ Play'}
          </button>
        )}
        <button type="button" onClick={onStepBack} disabled={stepIndex === 0} aria-label="Step back" title="Step back (←)">
          ⏮
        </button>
        <button
          type="button"
          onClick={onStepForward}
          disabled={!hasSteps || stepIndex >= totalSteps}
          aria-label="Step forward"
          title="Step forward (→)"
        >
          ⏭
        </button>
        <button type="button" onClick={onReset} disabled={!hasSteps} aria-label="Reset" title="Reset">
          ↺
        </button>
      </div>

      <div className="field-row">
      <label className="field">
        <span>
          Step <output>{hasSteps ? `${stepIndex} / ${totalSteps}` : '—'}</output>
        </span>
        <input
          type="range"
          min={0}
          max={totalSteps}
          value={stepIndex}
          onChange={(e) => onSeek(Number(e.target.value))}
          disabled={!hasSteps}
        />
      </label>

      <label className="field">
        <span>
          Speed <output>{speed}x</output>
        </span>
        <input
          type="range"
          min={MIN_SPEED}
          max={MAX_SPEED}
          step={SPEED_STEP}
          value={speed}
          onChange={(e) => onSpeedChange(Number(e.target.value))}
        />
      </label>
      </div>
    </section>
  )
}
