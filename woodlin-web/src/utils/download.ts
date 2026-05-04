/**
 * @file utils/download.ts
 * @description 浏览器端文件下载工具
 * @author yulin
 * @since 2026-05-04
 */

/**
 * 触发浏览器下载 Blob
 * @param blob 二进制内容
 * @param filename 下载文件名
 */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(url)
}

/**
 * 通过 URL 触发下载（同源）
 * @param url 资源地址
 * @param filename 文件名
 */
export function downloadByUrl(url: string, filename?: string): void {
  const link = document.createElement('a')
  link.href = url
  if (filename) {link.download = filename}
  link.target = '_blank'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}
