/**
 * @file directives/copy.ts
 * @description v-copy 指令：点击复制绑定值到剪贴板
 * @author yulin
 * @since 2026-05-04
 */
import type { Directive } from 'vue'

interface CopyEl extends HTMLElement {
  __copy_handler__?: (e: Event) => void
  __copy_value__?: string
}

/** v-copy */
export const copy: Directive<CopyEl, string> = {
  mounted(el, binding) {
    el.__copy_value__ = binding.value
    el.__copy_handler__ = async () => {
      const text = el.__copy_value__ ?? ''
      try {
        await navigator.clipboard.writeText(text)
      } catch {
        const ta = document.createElement('textarea')
        ta.value = text
        document.body.appendChild(ta)
        ta.select()
        document.execCommand('copy')
        document.body.removeChild(ta)
      }
    }
    el.addEventListener('click', el.__copy_handler__)
  },
  updated(el, binding) {
    el.__copy_value__ = binding.value
  },
  unmounted(el) {
    if (el.__copy_handler__) {
      el.removeEventListener('click', el.__copy_handler__)
    }
  },
}
