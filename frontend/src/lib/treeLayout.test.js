import { describe, expect, it } from 'vitest'
import { layoutTree } from './treeLayout'

describe('layoutTree', () => {
  it('centres a parent over its children', () => {
    const { items, width, depth } = layoutTree(['0->1', '0->2'], 0, [0, 1, 2])
    const byId = Object.fromEntries(items.map((i) => [i.id, i]))
    expect(byId[0]).toMatchObject({ x: 0.5, y: 0, parent: null })
    expect(byId[1]).toMatchObject({ x: 0, y: 1, parent: 0 })
    expect(width).toBe(2)
    expect(depth).toBe(1)
  })

  it('handles undirected edges in either direction and separate components', () => {
    const { items } = layoutTree(['1->0', '3->2'], 0, [0, 1, 2, 3])
    expect(items).toHaveLength(4)
    expect(items.filter((i) => i.parent === null)).toHaveLength(2)
  })

  it('is empty without edges', () => {
    expect(layoutTree([], 0, [0, 1]).items).toEqual([])
  })
})
