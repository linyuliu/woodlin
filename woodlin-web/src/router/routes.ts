/**
 * 路由配置模块
 *
 * @author mumu
 * @description 定义应用的所有路由，包括静态路由和动态路由
 *              参考vue-vben-admin的路由设计，支持权限控制和动态路由
 * @since 2025-01-01
 */

import type {RouteRecordRaw} from 'vue-router'
import {LAYOUT} from './route-constants'

/**
 * 静态路由（不需要权限的基础路由）
 *
 * 这些路由对所有用户可见，不需要权限验证
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: {
      title: '登录',
      hideInMenu: true,
      anonymous: true, // 允许匿名访问
    },
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: {
      title: '无权限',
      hideInMenu: true,
    },
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: {
      title: '页面不存在',
      hideInMenu: true,
    },
  },
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/500.vue'),
    meta: {
      title: '服务器错误',
      hideInMenu: true,
    },
  },
]

/**
 * 动态路由（需要根据权限动态加载的路由）
 *
 * 这些路由会根据用户权限进行过滤，只有有权限的用户才能访问
 */
export const asyncRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: LAYOUT,
    redirect: '/dashboard',
    children: [
      // ===== 仪表板 =====
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: {
          title: '仪表板',
          icon: 'speedometer-outline',
          affix: true, // 固定在标签页
          permissions: ['dashboard:view'],
        },
      },

      // ===== 系统管理 =====
      {
        path: 'system',
        name: 'System',
        redirect: '/system/user',
        meta: {
          title: '系统管理',
          icon: 'settings-outline',
          permissions: ['system:view'],
        },
        children: [
          {
            path: 'user',
            name: 'SystemUser',
            component: () => import('@/views/system/UserView.vue'),
            meta: {
              title: '用户管理',
              icon: 'people-outline',
              permissions: ['system:user:view'],
            },
          },
          {
            path: 'role',
            name: 'SystemRole',
            component: () => import('@/views/system/RoleView.vue'),
            meta: {
              title: '角色管理',
              icon: 'shield-checkmark-outline',
              permissions: ['system:role:view'],
            },
          },
          {
            path: 'dept',
            name: 'SystemDept',
            component: () => import('@/views/system/DeptView.vue'),
            meta: {
              title: '部门管理',
              icon: 'business-outline',
              permissions: ['system:dept:view'],
            },
          },
          {
            path: 'menu',
            name: 'SystemMenu',
            component: () => import('@/views/system/MenuView.vue'),
            meta: {
              title: '菜单管理',
              icon: 'list-outline',
              permissions: ['system:menu:view'],
            },
          },
          {
            path: 'permission',
            name: 'SystemPermission',
            component: () => import('@/views/system/permission/PermissionManagementView.vue'),
            meta: {
              title: '权限管理',
              icon: 'key-outline',
              permissions: ['system:permission:view'],
            },
          },
          {
            path: 'dict',
            name: 'SystemDict',
            component: () => import('@/views/system/DictView.vue'),
            meta: {
              title: '字典管理',
              icon: 'book-outline',
              permissions: ['system:dict:view'],
            },
          },
          {
            path: 'config',
            name: 'SystemConfig',
            component: () => import('@/views/system/ConfigView.vue'),
            meta: {
              title: '配置管理',
              icon: 'options-outline',
              permissions: ['system:config:view'],
            },
          },
          {
            path: 'settings',
            name: 'SystemSettings',
            component: () => import('@/views/system/SystemSettingsView.vue'),
            meta: {
              title: '系统设置',
              icon: 'cog-outline',
              permissions: ['system:settings:view'],
            },
          },
        ],
      },

      // ===== 数据源管理 =====
      {
        path: 'datasource',
        name: 'Datasource',
        redirect: '/datasource/list',
        meta: {
          title: '数据源管理',
          icon: 'server-outline',
          permissions: ['datasource:view'],
        },
        children: [
          {
            path: 'list',
            name: 'DatasourceList',
            component: () => import('@/views/datasource/DatasourceList.vue'),
            meta: {
              title: '数据源列表',
              icon: 'list-outline',
              permissions: ['datasource:list:view'],
            },
          },
          {
            path: 'monitor',
            name: 'DatasourceMonitor',
            component: () => import('@/views/datasource/DatasourceMonitor.vue'),
            meta: {
              title: '数据源监控',
              icon: 'stats-chart-outline',
              permissions: ['datasource:monitor:view'],
            },
          },
          {
            path: 'workspace/:code',
            name: 'DatasourceWorkspace',
            component: () => import('@/views/datasource/DatasourceWorkspace.vue'),
            meta: {
              title: '元数据工作台',
              hideInMenu: true,
              activeMenu: '/datasource/list',
              permissions: ['datasource:list:view'],
            },
          },
        ],
      },

      // ===== 租户管理 =====
      {
        path: 'tenant',
        name: 'Tenant',
        redirect: '/tenant/list',
        meta: {
          title: '租户管理',
          icon: 'home-outline',
          permissions: ['tenant:view'],
        },
        children: [
          {
            path: 'list',
            name: 'TenantList',
            component: () => import('@/views/tenant/TenantView.vue'),
            meta: {
              title: '租户列表',
              icon: 'list-outline',
              permissions: ['tenant:list:view'],
            },
          },
        ],
      },

      // ===== 文件管理 =====
      {
        path: 'file',
        name: 'File',
        redirect: '/file/list',
        meta: {
          title: '文件管理',
          icon: 'folder-outline',
          permissions: ['file:view'],
        },
        children: [
          {
            path: 'list',
            name: 'FileList',
            component: () => import('@/views/file/FileList.vue'),
            meta: {
              title: '文件列表',
              icon: 'documents-outline',
              permissions: ['file:list:view'],
            },
          },
          {
            path: 'storage',
            name: 'FileStorage',
            component: () => import('@/views/file/FileStorage.vue'),
            meta: {
              title: '存储配置',
              icon: 'archive-outline',
              permissions: ['file:storage:view'],
            },
          },
        ],
      },

      // ===== 任务管理 =====
      {
        path: 'task',
        name: 'Task',
        redirect: '/task/list',
        meta: {
          title: '任务管理',
          icon: 'timer-outline',
          permissions: ['task:view'],
        },
        children: [
          {
            path: 'list',
            name: 'TaskList',
            component: () => import('@/views/task/TaskList.vue'),
            meta: {
              title: '任务列表',
              icon: 'list-outline',
              permissions: ['task:list:view'],
            },
          },
          {
            path: 'log',
            name: 'TaskLog',
            component: () => import('@/views/task/TaskLog.vue'),
            meta: {
              title: '任务日志',
              icon: 'receipt-outline',
              permissions: ['task:log:view'],
            },
          },
        ],
      },

      // ===== ETL离线同步 =====
      {
        path: 'etl',
        name: 'Etl',
        redirect: '/etl/offline',
        meta: {
          title: '同步任务',
          icon: 'git-compare-outline',
          permissions: ['task:list:view'],
        },
        children: [
          {
            path: 'offline',
            name: 'EtlOfflineTaskList',
            component: () => import('@/views/etl/EtlOfflineTaskList.vue'),
            meta: {
              title: '离线同步',
              icon: 'swap-horizontal-outline',
              permissions: ['task:list:view'],
            },
          },
          {
            path: 'offline/create',
            name: 'EtlOfflineCreate',
            component: () => import('@/views/etl/EtlOfflineCreate.vue'),
            meta: {
              title: '创建离线任务',
              hideInMenu: true,
              activeMenu: '/etl/offline',
              permissions: ['task:list:view'],
            },
          },
        ],
      },

      // ===== 开发工具 =====
      {
        path: 'dev',
        name: 'Dev',
        redirect: '/dev/sql2api',
        meta: {
          title: '开发工具',
          icon: 'code-slash-outline',
          permissions: ['dev:view'],
        },
        children: [
          {
            path: 'sql2api',
            name: 'SqlApiEditor',
            component: () => import('@/views/sql2api/SqlApiEditor.vue'),
            meta: {
              title: 'SQL转API',
              icon: 'terminal-outline',
              permissions: ['dev:sql2api:view'],
            },
          },
          {
            path: 'generator',
            name: 'CodeGenerator',
            component: () => import('@/views/dev/CodeGenerator.vue'),
            meta: {
              title: '代码生成',
              icon: 'construct-outline',
              permissions: ['dev:generator:view'],
            },
          },
        ],
      },
    ],
  },
]

/**
 * 404 catch-all route
 * NOTE: This must be added LAST after all dynamic routes are registered
 */
export const notFoundRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  redirect: '/404',
  meta: {
    hideInMenu: true,
  },
}

/**
 * 所有路由（用于调试和测试）
 */
export const allRoutes = [...constantRoutes, ...asyncRoutes, notFoundRoute]
