/**
 * @file composables/useDownload.ts
 * @description 下载文件 composable，封装 loading + 错误提示
 * @author yulin
 * @since 2026-05-04
 */
import { ref } from 'vue'
import { downloadBlob } from '@/utils/download'
import service from '@/utils/request'

/** 下载 hook */
export function useDownload() {
  const loading = ref(false)

  /**
   * 通过 GET 拉取 blob 并保存
   * @param url 后端 URL
   * @param filename 默认文件名
   * @param params 查询参数
   */
  async function download(url: string, filename: string, params?: Record<string, unknown>): Promise<void> {
    loading.value = true
    try {
      const res = await service.get(url, { params, responseType: 'blob' })
      const blob = res.data instanceof Blob ? res.data : new Blob([res.data])
      downloadBlob(blob, filename)
    } finally {
      loading.value = false
    }
  }

  return { loading, download }
}
