/**
 * @file utils/tree.ts
 * @description 列表/树结构互转、路径查找等工具
 * @author yulin
 * @since 2026-05-04
 */

export interface TreeNode {
  id: number | string
  parentId: number | string
  children?: TreeNode[]
  [key: string]: unknown
}

/**
 * 扁平列表转树
 * @param list 列表数据
 * @param rootId 根节点 parentId 值，默认 0
 */
export function listToTree<T extends TreeNode>(list: T[], rootId: number | string = 0): T[] {
  const map = new Map<number | string, T>()
  const roots: T[] = []
  list.forEach((item) => map.set(item.id, { ...item, children: [] }))
  map.forEach((item) => {
    if (item.parentId === rootId) {
      roots.push(item)
    } else {
      const parent = map.get(item.parentId)
      if (parent) {
        ;(parent.children as T[]).push(item)
      } else {
        roots.push(item)
      }
    }
  })
  return roots
}

/**
 * 在树中查找符合条件的节点路径
 * @param tree 树
 * @param predicate 匹配函数
 */
export function findPath<T extends TreeNode>(
  tree: T[],
  predicate: (node: T) => boolean,
): T[] | null {
  for (const node of tree) {
    if (predicate(node)) return [node]
    if (node.children && node.children.length) {
      const sub = findPath(node.children as T[], predicate)
      if (sub) return [node, ...sub]
    }
  }
  return null
}
