/**
 * 菜单配置模块
 * 
 * @author mumu
 * @description 定义应用的菜单结构
 * 
 * TODO: 将菜单改为从路由动态生成，参考 utils/menu-generator.ts
 * 建议在 AdminLayout 中使用：
 * 
 * ```typescript
 * import { usePermissionStore } from '@/stores'
 * import { generateMenuFromRoutes } from '@/utils/menu-generator'
 * 
 * const permissionStore = usePermissionStore()
 * const menuOptions = computed(() => 
 *   generateMenuFromRoutes(permissionStore.menuRoutes)
 * )
 * ```
 * 
 * @since 2025-01-01
 */

import { h, type Component } from 'vue'
import { NIcon, type MenuOption } from 'naive-ui'
import {
  HomeOutline,
  PeopleOutline,
  ShieldCheckmarkOutline,
  BusinessOutline,
  SettingsOutline,
  AppsOutline,
  KeyOutline,
  CodeSlashOutline
} from '@vicons/ionicons5'

export interface AppMenuItem {
  label: string
  key: string
  icon?: MenuOption['icon']
  children?: AppMenuItem[]
}

const renderIcon = (icon: Component) => {
  return () => h(NIcon, null, { default: () => h(icon) })
}

export const appMenuItems: AppMenuItem[] = [
  {
    label: '仪表板',
    key: '/dashboard',
    icon: renderIcon(HomeOutline)
  },
  {
    label: '系统管理',
    key: 'system',
    icon: renderIcon(AppsOutline),
    children: [
      {
        label: '用户管理',
        key: '/user',
        icon: renderIcon(PeopleOutline)
      },
      {
        label: '角色管理',
        key: '/role',
        icon: renderIcon(ShieldCheckmarkOutline)
      },
      {
        label: '部门管理',
        key: '/dept',
        icon: renderIcon(BusinessOutline)
      },
      {
        label: '权限管理',
        key: '/permission',
        icon: renderIcon(KeyOutline)
      },
      {
        label: '系统设置',
        key: '/system-settings',
        icon: renderIcon(SettingsOutline)
      }
    ]
  },
  {
    label: '开发工具',
    key: 'dev-tools',
    icon: renderIcon(CodeSlashOutline),
    children: [
      {
        label: 'SQL转API',
        key: '/sql2api',
        icon: renderIcon(CodeSlashOutline)
      }
    ]
  },
  {
    label: '租户管理',
    key: 'tenant',
    icon: renderIcon(BusinessOutline),
    children: [
      {
        label: '租户列表',
        key: '/tenant-list',
        icon: renderIcon(AppsOutline)
      }
    ]
  }
]

export const toNaiveMenuOptions = (items: AppMenuItem[]): MenuOption[] => {
  const mapOptions = (source: AppMenuItem[]): MenuOption[] =>
    source.map((item) => {
      const option: MenuOption = {
        key: item.key,
        label: item.label,
        icon: item.icon
      }

      if (item.children?.length) {
        option.children = mapOptions(item.children)
      }

      return option
    })

  return mapOptions(items)
}
