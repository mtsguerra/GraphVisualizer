import { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import AlgorithmSelector from './components/AlgorithmSelector'
import Controls from './components/Controls'
import GraphEditor from './components/GraphEditor'
import StatsPanel from './components/StatsPanel'
import Visualizer from './components/Visualizer'
import { SAMPLE_END, SAMPLE_GRAPH, SAMPLE_START } from './data/sampleGraph'
import { ALGORITHMS, BASE_STEP_MS, DEFAULT_SPEED } from './lib/constants'
import { computeVisualState } from './lib/stepState'
import { fetchSampleGraph, runAlgorithm } from './services/api'
import './App.css'

const EMPTY_STEPS = []

/** 0 -> A, 25 -> Z, 26 -> AA, ... */
function labelFor(index) {
  let label = ''
  for (let n = index; n >= 0; n = Math.floor(n / 26) - 1) {
    label = String.fromCharCode(65 + (n % 26)) + label
  }
  return label
}

export default function App() {
  const [graph, setGraph] = useState(SAMPLE_GRAPH)
  const [startNode, setStartNode] = useState(SAMPLE_START)
  const [endNode, setEndNode] = useState(SAMPLE_END)
  const [algorithm, setAlgorithm] = useState('bfs')

  const [result, setResult] = useState(null)
  const [stepIndex, setStepIndex] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [speed, setSpeed] = useState(DEFAULT_SPEED)
  const [loading, setLoading] = useState(false)
  const [requestMs, setRequestMs] = useState(null)
  const [error, setError] = useState(null)
  const [notice, setNotice] = useState(null)

  const [editMode, setEditMode] = useState('move')
  const [pickMode, setPickMode] = useState(null)
  const [pendingNode, setPendingNode] = useState(null)
  const [edgeWeight, setEdgeWeight] = useState('1')
  const [fitVersion, setFitVersion] = useState(0)

  // Lets us drop responses for a graph the user has since changed.
  const requestIdRef = useRef(0)

  const steps = result?.steps ?? EMPTY_STEPS
  const visual = useMemo(() => computeVisualState(steps, stepIndex), [steps, stepIndex])
  const needsEnd = ALGORITHMS.find((a) => a.id === algorithm).needsEnd
  const finished = result != null && stepIndex >= steps.length
  const labelOf = (id) => graph.nodes.find((n) => n.id === id)?.label ?? id
  const pathText = finished && result.path?.length > 0 ? result.path.map(labelOf).join(' → ') : null
  const canPlay = startNode != null && (!needsEnd || endNode != null)

  /** Any change to the inputs makes the current run stale. */
  const invalidate = useCallback(() => {
    requestIdRef.current++
    setResult(null)
    setStepIndex(0)
    setPlaying(false)
    setLoading(false)
    setRequestMs(null)
    setError(null)
  }, [])

  const loadGraph = useCallback(
    ({ nodes, edges, directed = false, startNode: start = null, endNode: end = null }) => {
      invalidate()
      setGraph({ nodes, edges, directed })
      setStartNode(start)
      setEndNode(end)
      setPendingNode(null)
      setFitVersion((v) => v + 1)
    },
    [invalidate],
  )

  // Show the backend's sample graph when it's reachable; otherwise keep the built-in copy.
  useEffect(() => {
    let ignore = false
    fetchSampleGraph()
      .then((sample) => {
        if (!ignore) loadGraph(sample)
      })
      .catch(() => {
        if (!ignore) setNotice('Backend not reachable yet: showing the built-in sample graph.')
      })
    return () => {
      ignore = true
    }
  }, [loadGraph])

  // Animation clock: advance one step per tick while playing.
  useEffect(() => {
    if (!playing) return undefined
    const timer = setTimeout(() => {
      const next = stepIndex + 1
      setStepIndex(next)
      if (next >= steps.length) setPlaying(false)
    }, BASE_STEP_MS / speed)
    return () => clearTimeout(timer)
  }, [playing, stepIndex, speed, steps.length])

  async function handlePlay() {
    setError(null)
    if (result) {
      if (stepIndex >= steps.length) setStepIndex(0)
      setPlaying(true)
      return
    }
    const requestId = ++requestIdRef.current
    setLoading(true)
    const startedAt = performance.now()
    try {
      const response = await runAlgorithm(algorithm, { graph, startNode, endNode })
      if (requestId !== requestIdRef.current) return
      setRequestMs(performance.now() - startedAt)
      setResult(response)
      setStepIndex(0)
      setPlaying(response.steps.length > 0)
      setNotice(null)
    } catch (e) {
      if (requestId === requestIdRef.current) setError(e.message)
    } finally {
      if (requestId === requestIdRef.current) setLoading(false)
    }
  }

  function updateGraph(update) {
    invalidate()
    setGraph((g) => ({ ...g, ...update(g) }))
  }

  function handleBackgroundTap({ x, y }) {
    if (editMode === 'addNode') {
      const id = graph.nodes.reduce((max, n) => Math.max(max, n.id), -1) + 1
      const usedLabels = new Set(graph.nodes.map((n) => n.label))
      let i = 0
      while (usedLabels.has(labelFor(i))) i++
      updateGraph((g) => ({ nodes: [...g.nodes, { id, label: labelFor(i), x: Math.round(x), y: Math.round(y) }] }))
      if (startNode == null) setStartNode(id)
    } else if (editMode === 'addEdge') {
      setPendingNode(null)
    }
  }

  function handleNodeTap(id) {
    if (pickMode) {
      invalidate()
      if (pickMode === 'start') setStartNode(id)
      else setEndNode(id)
      setPickMode(null)
      return
    }
    if (editMode === 'addEdge') {
      if (pendingNode == null) {
        setPendingNode(id)
        return
      }
      if (pendingNode !== id) {
        const weight = Number(edgeWeight)
        const from = pendingNode
        updateGraph((g) => ({
          edges: [...g.edges, { from, to: id, weight: Number.isFinite(weight) && weight >= 0 ? weight : 1 }],
        }))
      }
      setPendingNode(null)
    } else if (editMode === 'delete') {
      updateGraph((g) => ({
        nodes: g.nodes.filter((n) => n.id !== id),
        edges: g.edges.filter((e) => e.from !== id && e.to !== id),
      }))
      if (startNode === id) setStartNode(graph.nodes.find((n) => n.id !== id)?.id ?? null)
      if (endNode === id) setEndNode(null)
    }
  }

  function handleEdgeTap(index) {
    if (editMode === 'delete') {
      updateGraph((g) => ({ edges: g.edges.filter((_, i) => i !== index) }))
    }
  }

  function handleNodeMoved(id, x, y) {
    // Positions only feed A*'s heuristic, so other runs can keep playing.
    if (algorithm === 'astar') invalidate()
    setGraph((g) => ({ ...g, nodes: g.nodes.map((n) => (n.id === id ? { ...n, x, y } : n)) }))
  }

  // Esc cancels a pending Set start / Set end.
  useEffect(() => {
    if (!pickMode) return undefined
    const onKey = (e) => e.key === 'Escape' && setPickMode(null)
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [pickMode])

  function handleModeChange(mode) {
    setPickMode(null)
    setEditMode(mode)
    setPendingNode(null)
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Graph <span>Visualizer</span></h1>
        {notice && <p className="notice">{notice}</p>}
        {error && (
          <p className="error" role="alert">
            {error}
          </p>
        )}
      </header>

      <div className="toolbar">
        <AlgorithmSelector
          algorithm={algorithm}
          onAlgorithmChange={(a) => {
            invalidate()
            setAlgorithm(a)
          }}
          nodes={graph.nodes}
          startNode={startNode}
          endNode={endNode}
          onStartChange={(id) => {
            invalidate()
            setStartNode(id)
          }}
          onEndChange={(id) => {
            invalidate()
            setEndNode(id)
          }}
          pickMode={pickMode}
          onPickModeChange={setPickMode}
          pathText={pathText}
          disabled={loading}
        />
        <GraphEditor
          mode={editMode}
          onModeChange={handleModeChange}
          edgeWeight={edgeWeight}
          onEdgeWeightChange={setEdgeWeight}
          directed={graph.directed}
          onDirectedChange={(directed) => updateGraph(() => ({ directed }))}
          onLoadSample={() => loadGraph({ ...SAMPLE_GRAPH, startNode: SAMPLE_START, endNode: SAMPLE_END })}
          onClear={() => loadGraph({ nodes: [], edges: [], directed: graph.directed })}
          disabled={loading}
        />
        <Controls
          playing={playing}
          loading={loading}
          canPlay={canPlay}
          stepIndex={stepIndex}
          totalSteps={steps.length}
          speed={speed}
          onPlay={handlePlay}
          onPause={() => setPlaying(false)}
          onStepBack={() => {
            setPlaying(false)
            setStepIndex((i) => Math.max(0, i - 1))
          }}
          onStepForward={() => {
            setPlaying(false)
            setStepIndex((i) => Math.min(steps.length, i + 1))
          }}
          onReset={() => {
            setPlaying(false)
            setStepIndex(0)
          }}
          onSeek={(i) => {
            setPlaying(false)
            setStepIndex(i)
          }}
          onSpeedChange={setSpeed}
        />
      </div>

      <aside className="sidebar">
        <StatsPanel
          result={result}
          visual={visual}
          stepIndex={stepIndex}
          totalNodes={graph.nodes.length}
          requestMs={requestMs}
          nodes={graph.nodes}
        />
      </aside>

      <main className="canvas">
        <Visualizer
          graph={graph}
          startNode={startNode}
          endNode={endNode}
          visual={visual}
          editMode={editMode}
          pickMode={pickMode}
          pendingNode={pendingNode}
          fitVersion={fitVersion}
          onBackgroundTap={handleBackgroundTap}
          onNodeTap={handleNodeTap}
          onEdgeTap={handleEdgeTap}
          onNodeMoved={handleNodeMoved}
        />
      </main>

      <aside className="tree-panel" aria-label="Tree view">
        <section className="panel">
          <h2>Tree</h2>
          <p className="hint">Tree view coming soon.</p>
        </section>
      </aside>
    </div>
  )
}
