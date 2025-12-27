/**
 * 将字节数格式化为更友好的单位（B/KB/MB/GB/TB）。
 *
 * - input 为空或非法时返回空字符串
 * - 默认保留 2 位小数（可配置）
 */
export function formatBytes(input?: number | string | null, decimals: number = 2): string {
  if (input == null || input === '')
    return ''

  const bytes = typeof input === 'string' ? Number(input) : input
  if (!Number.isFinite(bytes))
    return ''

  if (bytes === 0)
    return '0 B'

  const k = 1024
  const dm = Math.max(0, decimals)
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']

  const i = Math.min(Math.floor(Math.log(bytes) / Math.log(k)), sizes.length - 1)
  const value = bytes / Math.pow(k, i)

  // 去掉多余的 0，比如 1.00 -> 1
  const fixed = value.toFixed(dm)
  const normalized = dm === 0 ? fixed : fixed.replace(/\.0+$|(?<=\.\d*[1-9])0+$/g, '')

  return `${normalized} ${sizes[i]}`
}
