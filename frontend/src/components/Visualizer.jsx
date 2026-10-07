import cytoscape from 'cytoscape'
import { useEffect, useRef } from 'react'
import { COLORS } from '../lib/constants'
import { edgeStateFor, formatDistance } from '../lib/stepState'

const NODE_STATES = ['exploring', 'current', 'visited', 'path']
const NODE_CLASSES = [...NODE_STATES, 'start', 'end', 'pending']
const PULSE_FPS_MS = 33

const STYLE = [
  {
    selector: 'node',
    style: {
      'background-color': COLORS.unvisited,
      label: 'data(display)',
      color: COLORS.text,
      'font-size': 13,
      'font-weight': 600,
      'text-valign': 'center',
      'text-halign': 'center',
      'text-wrap': 'wrap',
      width: 54,
      height: 54,
      'border-width': 2,
      'border-color': COLORS.unvisitedBorder,
      'underlay-color': COLORS.exploring,
      'underlay-padding': 6,
      'underlay-opacity': 0,
      'transition-property': 'background-color, border-color, width, height',
      'transition-duration': 250,
    },
  },
  {
    selector: 'node.exploring',
    style: { 'border-color': COLORS.exploring, 'border-width': 3, 'underlay-color': COLORS.exploring, 'underlay-opacity': 'data(pulse)' },
  },
  { selector: 'node.visited', style: { 'background-color': COLORS.visited, 'border-color': COLORS.visitedBorder } },
  {
    selector: 'node.current',
    style: {
      'background-color': COLORS.current,
      'border-color': '#fde68a',
      color: COLORS.ink,
      width: 62,
      height: 62,
      'underlay-color': COLORS.current,
      'underlay-opacity': 'data(pulse)',
    },
  },
  { selector: 'node.path', style: { 'background-color': COLORS.path, 'border-color': '#ddd6fe', color: COLORS.ink } },
  { selector: 'node.start', style: { 'border-width': 5, 'border-color': COLORS.start } },
  { selector: 'node.end', style: { 'border-width': 5, 'border-color': COLORS.end } },
  { selector: 'node.pending', style: { 'underlay-color': COLORS.exploring, 'underlay-opacity': 0.35 } },
  {
    selector: 'edge',
    style: {
      width: 2,
      'line-color': COLORS.edge,
      'target-arrow-color': COLORS.edge,
      'curve-style': 'bezier',
      label: 'data(weight)',
      'font-size': 12,
      color: COLORS.edgeLabel,
      'text-background-color': COLORS.canvas,
      'text-background-opacity': 1,
      'text-background-padding': 3,
      'transition-property': 'line-color, target-arrow-color, width',
      'transition-duration': 300,
    },
  },
  { selector: 'edge.directed', style: { 'target-arrow-shape': 'triangle' } },
  { selector: 'edge.tree', style: { width: 3.5, 'line-color': COLORS.exploring, 'target-arrow-color': COLORS.exploring } },
  {
    selector: 'edge.path',
    style: {
      width: 5,
      'line-color': COLORS.path,
      'target-arrow-color': COLORS.path,
      'line-style': 'dashed',
      'line-dash-pattern': [12, 6],
      'line-dash-offset': 'data(dash)',
    },
  },
]

/**
 * Cytoscape canvas. The graph is owned by App; this component mirrors it and
 * reports user interactions back through the on* callbacks.
 */
export default function Visualizer({
  graph,
  startNode,
  endNode,
  visual,
  editMode,
  pickMode,
  pendingNode,
  fitVersion,
  onBackgroundTap,
  onNodeTap,
  onEdgeTap,
  onNodeMoved,
}) {
  const containerRef = useRef(null)
  const cyRef = useRef(null)
  // Cytoscape listeners are registered once, so they read the latest callbacks from a ref.
  const handlersRef = useRef({})

  useEffect(() => {
    handlersRef.current = { onBackgroundTap, onNodeTap, onEdgeTap, onNodeMoved }
  })

  useEffect(() => {
    const cy = cytoscape({
      container: containerRef.current,
      style: STYLE,
      layout: { name: 'preset' },
      minZoom: 0.2,
      maxZoom: 3,
      boxSelectionEnabled: false,
      autounselectify: true,
    })
    cy.on('tap', (event) => {
      if (event.target === cy) handlersRef.current.onBackgroundTap?.(event.position)
    })
    cy.on('tap', 'node', (event) => handlersRef.current.onNodeTap?.(Number(event.target.id())))
    cy.on('tap', 'edge', (event) => handlersRef.current.onEdgeTap?.(event.target.data('index')))
    cy.on('dragfree', 'node', (event) => {
      const { x, y } = event.target.position()
      handlersRef.current.onNodeMoved?.(Number(event.target.id()), Math.round(x), Math.round(y))
    })
    cyRef.current = cy
    return () => cy.destroy()
  }, [])

  // Rebuild elements whenever the graph itself changes.
  useEffect(() => {
    const cy = cyRef.current
    cy.batch(() => {
      cy.elements().remove()
      cy.add(
        graph.nodes.map((node) => ({
          group: 'nodes',
          data: { id: String(node.id), label: node.label, display: node.label, pulse: 0 },
          position: { x: node.x, y: node.y },
        })),
      )
      cy.add(
        graph.edges.map((edge, index) => ({
          group: 'edges',
          data: {
            id: `e${index}`,
            index,
            source: String(edge.from),
            target: String(edge.to),
            weight: edge.weight,
            dash: 0,
          },
          classes: graph.directed ? 'directed' : '',
        })),
      )
    })
  }, [graph])

  useEffect(() => {
    const cy = cyRef.current
    cy.fit(undefined, 40)
    if (cy.zoom() > 1.5) {
      cy.zoom(1.5)
      cy.center()
    }
  }, [fitVersion])

  // Apply animation state on top of the elements.
  useEffect(() => {
    const cy = cyRef.current
    cy.batch(() => {
      cy.nodes().forEach((element) => {
        const id = Number(element.id())
        element.removeClass(NODE_CLASSES)
        const state = visual.nodes.get(id)
        if (state) element.addClass(state)
        if (id === startNode) element.addClass('start')
        if (id === endNode) element.addClass('end')
        if (id === pendingNode) element.addClass('pending')

        const label = element.data('label')
        const distance = visual.distances.get(id)
        const icon = id === startNode ? '▶ ' : id === endNode ? '◎ ' : ''
        element.data('display', `${icon}${label}${distance == null ? '' : `\n${formatDistance(distance)}`}`)
      })
      cy.edges().forEach((element) => {
        element.removeClass(['tree', 'path'])
        const state = edgeStateFor(graph.edges[element.data('index')], graph.directed, visual)
        if (state) element.addClass(state)
      })
    })
  }, [graph, visual, startNode, endNode, pendingNode])

  // Ambient motion (frontier/current pulse, marching path dashes). It only writes
  // cosmetic data, so it never touches algorithm or playback state.
  useEffect(() => {
    if (window.matchMedia?.('(prefers-reduced-motion: reduce)').matches) return undefined
    const cy = cyRef.current
    let frame
    let last = 0
    const tick = (now) => {
      if (now - last >= PULSE_FPS_MS) {
        last = now
        const pulse = 0.3 + 0.25 * Math.sin(now / 220)
        const dash = -(now / 40) % 18
        cy.batch(() => {
          cy.nodes('.exploring, .current').data('pulse', pulse)
          cy.edges('.path').data('dash', dash)
        })
      }
      frame = requestAnimationFrame(tick)
    }
    frame = requestAnimationFrame(tick)
    return () => cancelAnimationFrame(frame)
  }, [])

  useEffect(() => {
    cyRef.current.autoungrabify(editMode !== 'move' || pickMode != null)
  }, [editMode, pickMode])

  return <div ref={containerRef} className={`visualizer mode-${pickMode ? 'pick' : editMode}`} />
}
