/**
 * @file directives/permission.ts
 * @description v-permission 指令：缺少权限时移除元素
 * @author yulin
 * @since 2026-05-04
 */
import type { Directive, DirectiveBinding } from 'vue'
import { hasPermission } from '@/utils/permission'

/** v-permission */
export const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    if (!hasPermission(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  },
}
