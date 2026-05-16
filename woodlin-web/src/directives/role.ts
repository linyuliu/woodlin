/**
 * @file directives/role.ts
 * @description v-role 指令：缺少角色时移除元素
 * @author yulin
 * @since 2026-05-05
 */
import type { Directive, DirectiveBinding } from 'vue'
import { hasRole } from '@/utils/permission'

/** v-role */
export const role: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    if (!hasRole(binding.value)) {
      el.parentNode?.removeChild(el)
    }
  },
}
