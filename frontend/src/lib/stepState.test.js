import { describe, expect, it } from 'vitest'
import { computeVisualState, edgeStateFor } from './stepState'

const steps = [
  { nodeId: 0, action: 'exploring', timestamp: 0, distance: 0 },
  { nodeId: 0, action: 'current', timestamp: 1, distance: 0 },
  { nodeId: 1, action: 'exploring', timestamp: 2, edge: { from: 0, to: 1, weight: 4 }, distance: 4 },
  { nodeId: 2, action: 'exploring', timestamp: 3, edge: { from: 0, to: 2, weight: 2 }, distance: 2 },
  { nodeId: 0, action: 'visited', timestamp: 4, distance: 0 },
  { nodeId: 2, action: 'current', timestamp: 5, edge: { from: 0, to: 2, weight: 2 }, distance: 2 },
  // B improves through C: its tree edge should move from 0->1 to 2->1.
  { nodeId: 1, action: 'exploring', timestamp: 6, edge: { from: 2, to: 1, weight: 1 }, distance: 3 },
  { nodeId: 0, action: 'path', timestamp: 7, distance: 0 },
  { nodeId: 2, action: 'path', timestamp: 8, edge: { from: 0, to: 2, weight: 2 }, distance: 2 },
]

describe('computeVisualState', () => {
  it('is empty before the first step', () => {
    const state = computeVisualState(steps, 0)
    expect(state.nodes.size).toBe(0)
    expect(state.lastStep).toBeNull()
  })

  it('keeps only the latest action and distance per node', () => {
    const state = computeVisualState(steps, 7)
    expect(state.nodes.get(0)).toBe('visited')
    expect(state.nodes.get(2)).toBe('current')
    expect(state.distances.get(1)).toBe(3)
    expect(state.visitedCount).toBe(2)
    expect(state.lastStep.timestamp).toBe(6)
  })

  it('tracks the most recent tree edge per node', () => {
    const state = computeVisualState(steps, 7)
    expect(state.edges.get('2->1')).toBe('tree')
    expect(state.edges.has('0->1')).toBe(false)
  })

  it('path overrides other states', () => {
    const state = computeVisualState(steps, steps.length)
    expect(state.nodes.get(2)).toBe('path')
    expect(state.edges.get('0->2')).toBe('path')
  })
})

describe('edgeStateFor', () => {
  const state = computeVisualState(steps, 7)

  it('matches undirected edges in either direction', () => {
    expect(edgeStateFor({ from: 1, to: 2 }, false, state)).toBe('tree')
  })

  it('matches directed edges only as drawn', () => {
    expect(edgeStateFor({ from: 1, to: 2 }, true, state)).toBeUndefined()
    expect(edgeStateFor({ from: 2, to: 1 }, true, state)).toBe('tree')
  })
})
