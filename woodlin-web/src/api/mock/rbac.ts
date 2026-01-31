/**
 * 简易前端 Mock（RBAC1）
 * - 纯内存实现，重启页面后重置
 * - 用 Promise + setTimeout 模拟网络
 */

export type Gender = 'male' | 'female' | 'unknown'

export interface DeptNode {
  deptId: number
  parentId: number | null
  deptName: string
  leader?: string
  phone?: string
  orderNum: number
  status: '1' | '0'
  children?: DeptNode[]
}

export interface RoleNode {
  roleId: number
  parentRoleId: number | null
  roleName: string
  roleCode: string
  sortOrder: number
  status: '1' | '0'
  remark?: string
  children?: RoleNode[]
}

export interface MenuNode {
  menuId: number
  parentId: number | null
  menuName: string
  icon?: string
  orderNum: number
  path?: string
  component?: string
  type: 'C' | 'M' | 'F' // C:目录 M:菜单 F:按钮
  perms?: string
  visible: '1' | '0'
  status: '1' | '0'
  children?: MenuNode[]
}

export interface UserItem {
  userId: number
  username: string
  nickname?: string
  email?: string
  mobile?: string
  gender?: Gender
  status: '1' | '0'
  deptId?: number
  roleIds: number[]
  remark?: string
  createTime?: string
}

let deptSeq = 1100
let roleSeq = 1200
let menuSeq = 1300
let userSeq = 1400

let deptTree: DeptNode[] = [
  {
    deptId: 1,
    parentId: null,
    deptName: '集团',
    orderNum: 1,
    status: '1',
    children: [
      {
        deptId: 10,
        parentId: 1,
        deptName: '研发中心',
        orderNum: 1,
        status: '1',
        children: [
          { deptId: 11, parentId: 10, deptName: '前端组', orderNum: 1, status: '1' },
          { deptId: 12, parentId: 10, deptName: '后端组', orderNum: 2, status: '1' }
        ]
      },
      {
        deptId: 20,
        parentId: 1,
        deptName: '运营中心',
        orderNum: 2,
        status: '1'
      }
    ]
  }
]

let roleTree: RoleNode[] = [
  {
    roleId: 1,
    parentRoleId: null,
    roleName: '超级管理员',
    roleCode: 'super_admin',
    sortOrder: 1,
    status: '1',
    remark: '拥有所有权限'
  },
  {
    roleId: 2,
    parentRoleId: null,
    roleName: '系统管理员',
    roleCode: 'sys_admin',
    sortOrder: 2,
    status: '1',
    remark: '系统模块全权限',
    children: [
      {
        roleId: 3,
        parentRoleId: 2,
        roleName: '运维',
        roleCode: 'ops',
        sortOrder: 1,
        status: '1'
      }
    ]
  },
  {
    roleId: 4,
    parentRoleId: null,
    roleName: '普通用户',
    roleCode: 'user',
    sortOrder: 99,
    status: '1'
  }
]

let menuTree: MenuNode[] = [
  {
    menuId: 1,
    parentId: null,
    menuName: '系统管理',
    icon: 'settings-outline',
    orderNum: 1,
    type: 'C',
    visible: '1',
    status: '1',
    children: [
      {
        menuId: 11,
        parentId: 1,
        menuName: '用户管理',
        icon: 'people-outline',
        orderNum: 1,
        type: 'M',
        path: '/system/user',
        component: 'system/UserView',
        perms: 'system:user:view',
        visible: '1',
        status: '1'
      },
      {
        menuId: 12,
        parentId: 1,
        menuName: '角色管理',
        icon: 'shield-outline',
        orderNum: 2,
        type: 'M',
        path: '/system/role',
        component: 'system/RoleView',
        perms: 'system:role:view',
        visible: '1',
        status: '1'
      },
      {
        menuId: 13,
        parentId: 1,
        menuName: '部门管理',
        icon: 'business-outline',
        orderNum: 3,
        type: 'M',
        path: '/system/dept',
        component: 'system/DeptView',
        perms: 'system:dept:view',
        visible: '1',
        status: '1'
      },
      {
        menuId: 14,
        parentId: 1,
        menuName: '菜单管理',
        icon: 'list-outline',
        orderNum: 4,
        type: 'M',
        path: '/system/menu',
        component: 'system/MenuView',
        perms: 'system:menu:view',
        visible: '1',
        status: '1'
      }
    ]
  }
]

let users: UserItem[] = [
  {
    userId: 1,
    username: 'super',
    nickname: '超管',
    email: 'super@woodlin.io',
    status: '1',
    deptId: 11,
    roleIds: [1],
    remark: '系统内置',
    createTime: '2024-01-01 10:00:00'
  },
  {
    userId: 2,
    username: 'admin',
    nickname: '管理员',
    email: 'admin@woodlin.io',
    status: '1',
    deptId: 10,
    roleIds: [2, 3],
    remark: '后台管理员',
    createTime: '2024-05-06 09:30:00'
  },
  {
    userId: 3,
    username: 'jane',
    nickname: 'Jane',
    email: 'jane@woodlin.io',
    status: '1',
    deptId: 20,
    roleIds: [4],
    remark: '运营',
    createTime: '2024-06-15 11:00:00'
  }
]

const delay = (ms = 120) => new Promise(resolve => setTimeout(resolve, ms))

const flatDeptIds = (nodes: DeptNode[]): number[] =>
  nodes.flatMap(n => [n.deptId, ...(n.children ? flatDeptIds(n.children) : [])])

// ========== Dept ==========
export async function fetchDeptTree(): Promise<DeptNode[]> {
  await delay()
  return JSON.parse(JSON.stringify(deptTree))
}

export async function createDept(payload: Partial<DeptNode>): Promise<DeptNode> {
  await delay()
  const newNode: DeptNode = {
    deptId: ++deptSeq,
    parentId: payload.parentId ?? null,
    deptName: payload.deptName || '新部门',
    leader: payload.leader,
    phone: payload.phone,
    orderNum: payload.orderNum ?? 1,
    status: payload.status ?? '1'
  }
  const insert = (list: DeptNode[]): boolean => {
    for (const node of list) {
      if (node.deptId === newNode.parentId) {
        node.children = node.children || []
        node.children.push(newNode)
        return true
      }
      if (node.children && insert(node.children)) return true
    }
    return false
  }
  if (newNode.parentId) insert(deptTree)
  else deptTree.push(newNode)
  return newNode
}

export async function updateDept(payload: DeptNode): Promise<DeptNode> {
  await delay()
  const update = (list: DeptNode[]) => {
    list.forEach(node => {
      if (node.deptId === payload.deptId) {
        Object.assign(node, payload)
      }
      if (node.children) update(node.children)
    })
  }
  update(deptTree)
  return payload
}

export async function deleteDept(deptId: number): Promise<boolean> {
  await delay()
  const remove = (list: DeptNode[]): DeptNode[] =>
    list
      .filter(n => n.deptId !== deptId)
      .map(n => ({ ...n, children: n.children ? remove(n.children) : undefined }))
  const ids = new Set(flatDeptIds(deptTree))
  if (!ids.has(deptId)) return false
  deptTree = remove(deptTree)
  return true
}

// ========== Role ==========
export async function fetchRoleTree(): Promise<RoleNode[]> {
  await delay()
  return JSON.parse(JSON.stringify(roleTree))
}

export async function createRole(payload: Partial<RoleNode>): Promise<RoleNode> {
  await delay()
  const newNode: RoleNode = {
    roleId: ++roleSeq,
    parentRoleId: payload.parentRoleId ?? null,
    roleName: payload.roleName || '新角色',
    roleCode: payload.roleCode || `role_${roleSeq}`,
    sortOrder: payload.sortOrder ?? 1,
    status: payload.status ?? '1',
    remark: payload.remark
  }
  const insert = (list: RoleNode[]): boolean => {
    for (const node of list) {
      if (node.roleId === newNode.parentRoleId) {
        node.children = node.children || []
        node.children.push(newNode)
        return true
      }
      if (node.children && insert(node.children)) return true
    }
    return false
  }
  if (newNode.parentRoleId) insert(roleTree)
  else roleTree.push(newNode)
  return newNode
}

export async function updateRole(payload: RoleNode): Promise<RoleNode> {
  await delay()
  const update = (list: RoleNode[]) =>
    list.forEach(node => {
      if (node.roleId === payload.roleId) Object.assign(node, payload)
      if (node.children) update(node.children)
    })
  update(roleTree)
  return payload
}

export async function deleteRole(roleId: number): Promise<boolean> {
  await delay()
  const remove = (list: RoleNode[]): RoleNode[] =>
    list
      .filter(n => n.roleId !== roleId)
      .map(n => ({ ...n, children: n.children ? remove(n.children) : undefined }))
  const ids = new Set(flatRoleIds(roleTree))
  if (!ids.has(roleId)) return false
  roleTree = remove(roleTree)
  users = users.map(u => ({ ...u, roleIds: u.roleIds.filter(id => id !== roleId) }))
  return true
}

const flatRoleIds = (nodes: RoleNode[]): number[] =>
  nodes.flatMap(n => [n.roleId, ...(n.children ? flatRoleIds(n.children) : [])])

// ========== Menu ==========
export async function fetchMenuTree(): Promise<MenuNode[]> {
  await delay()
  return JSON.parse(JSON.stringify(menuTree))
}

export async function createMenu(payload: Partial<MenuNode>): Promise<MenuNode> {
  await delay()
  const newNode: MenuNode = {
    menuId: ++menuSeq,
    parentId: payload.parentId ?? null,
    menuName: payload.menuName || '新菜单',
    icon: payload.icon || 'list-outline',
    orderNum: payload.orderNum ?? 1,
    type: payload.type || 'M',
    path: payload.path,
    component: payload.component,
    perms: payload.perms,
    visible: payload.visible ?? '1',
    status: payload.status ?? '1'
  }
  const insert = (list: MenuNode[]): boolean => {
    for (const node of list) {
      if (node.menuId === newNode.parentId) {
        node.children = node.children || []
        node.children.push(newNode)
        return true
      }
      if (node.children && insert(node.children)) return true
    }
    return false
  }
  if (newNode.parentId) insert(menuTree)
  else menuTree.push(newNode)
  return newNode
}

export async function updateMenu(payload: MenuNode): Promise<MenuNode> {
  await delay()
  const update = (list: MenuNode[]) =>
    list.forEach(node => {
      if (node.menuId === payload.menuId) Object.assign(node, payload)
      if (node.children) update(node.children)
    })
  update(menuTree)
  return payload
}

export async function deleteMenu(menuId: number): Promise<boolean> {
  await delay()
  const remove = (list: MenuNode[]): MenuNode[] =>
    list
      .filter(n => n.menuId !== menuId)
      .map(n => ({ ...n, children: n.children ? remove(n.children) : undefined }))
  const ids = new Set(flatMenuIds(menuTree))
  if (!ids.has(menuId)) return false
  menuTree = remove(menuTree)
  return true
}

const flatMenuIds = (nodes: MenuNode[]): number[] =>
  nodes.flatMap(n => [n.menuId, ...(n.children ? flatMenuIds(n.children) : [])])

// ========== User ==========
export interface UserQuery {
  username?: string
  nickname?: string
  status?: string
  deptId?: number
}

export async function fetchUserList(params: UserQuery = {}): Promise<UserItem[]> {
  await delay()
  return users.filter(u => {
    if (params.username && !u.username.includes(params.username)) return false
    if (params.nickname && !(u.nickname || '').includes(params.nickname)) return false
    if (params.status && u.status !== params.status) return false
    if (params.deptId && u.deptId !== params.deptId) return false
    return true
  })
}

export async function createUser(payload: Partial<UserItem>): Promise<UserItem> {
  await delay()
  const item: UserItem = {
    userId: ++userSeq,
    username: payload.username || `user_${userSeq}`,
    nickname: payload.nickname,
    email: payload.email,
    mobile: payload.mobile,
    gender: payload.gender || 'unknown',
    status: payload.status ?? '1',
    deptId: payload.deptId,
    roleIds: payload.roleIds || [],
    remark: payload.remark,
    createTime: new Date().toISOString()
  }
  users.unshift(item)
  return item
}

export async function updateUser(payload: UserItem): Promise<UserItem> {
  await delay()
  users = users.map(u => (u.userId === payload.userId ? { ...u, ...payload } : u))
  return payload
}

export async function deleteUsers(ids: number[]): Promise<boolean> {
  await delay()
  users = users.filter(u => !ids.includes(u.userId))
  return true
}
