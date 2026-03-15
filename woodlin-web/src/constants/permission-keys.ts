/**
 * 权限编码常量
 *
 * 统一维护路由权限与按钮/API权限编码，避免散落硬编码字符串。
 */
export const PERMISSIONS = {
  ROUTE: {
    DASHBOARD: 'dashboard',
    SYSTEM: 'system',
    SYSTEM_USER: 'system:user',
    SYSTEM_ROLE: 'system:role',
    SYSTEM_DEPT: 'system:dept',
    SYSTEM_MENU: 'system:menu',
    SYSTEM_DICT: 'system:dict',
    SYSTEM_CONFIG: 'system:config',
    SYSTEM_SETTINGS: 'system:settings',
    DATASOURCE: 'datasource',
    DATASOURCE_LIST: 'datasource:list',
    TENANT: 'tenant',
    TENANT_LIST: 'tenant:list',
    FILE: 'file',
    FILE_LIST: 'file:list',
    FILE_STORAGE: 'file:storage',
    TASK: 'task',
    TASK_LIST: 'task:list',
    TASK_LOG: 'task:log',
    DEV: 'dev',
    DEV_SQL2API: 'dev:sql2api',
    DEV_GENERATOR: 'dev:generator',
  },
  ACTION: {
    SYSTEM_USER_LIST: 'system:user:list',
    SYSTEM_USER_ADD: 'system:user:add',
    SYSTEM_USER_EDIT: 'system:user:edit',
    SYSTEM_USER_REMOVE: 'system:user:remove',
    SYSTEM_USER_RESET_PWD: 'system:user:resetPwd',
    SYSTEM_ROLE_LIST: 'system:role:list',
    SYSTEM_ROLE_ADD: 'system:role:add',
    SYSTEM_ROLE_EDIT: 'system:role:edit',
    SYSTEM_ROLE_REMOVE: 'system:role:remove',
    SYSTEM_DEPT_LIST: 'system:dept:list',
    SYSTEM_DEPT_ADD: 'system:dept:add',
    SYSTEM_DEPT_EDIT: 'system:dept:edit',
    SYSTEM_DEPT_REMOVE: 'system:dept:remove',
    SYSTEM_MENU_LIST: 'system:menu:list',
    SYSTEM_MENU_ADD: 'system:menu:add',
    SYSTEM_MENU_EDIT: 'system:menu:edit',
    SYSTEM_MENU_REMOVE: 'system:menu:remove',
    DATASOURCE_LIST: 'datasource:list',
    DATASOURCE_ADD: 'datasource:add',
    DATASOURCE_EDIT: 'datasource:edit',
    DATASOURCE_REMOVE: 'datasource:remove',
    DATASOURCE_TEST: 'datasource:test',
    DATASOURCE_METADATA: 'datasource:metadata',
  },
} as const

export type RoutePermissionKey = (typeof PERMISSIONS.ROUTE)[keyof typeof PERMISSIONS.ROUTE]
export type ActionPermissionKey = (typeof PERMISSIONS.ACTION)[keyof typeof PERMISSIONS.ACTION]
