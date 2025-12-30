/**
 * 将后端 ISO 时间字符串格式化为 YYYY-MM-DD。
 *
 * 支持形如：2025-12-27T02:46:48.000+00:00
 * 容错：null/undefined/空字符串/非法日期 -> 返回空字符串。
 */
export function formatToYMD(input?: string | null): string {
  if (!input)
    return ''

  // 绝大多数后端返回都是 ISO 8601，直接截取前 10 位最稳定且不受时区影响
  // 例如：2025-12-27T02:46:48.000+00:00 -> 2025-12-27
  if (/^\d{4}-\d{2}-\d{2}/.test(input))
    return input.slice(0, 10)

  // 兜底：尝试 Date 解析
  const d = new Date(input)
  if (Number.isNaN(d.getTime()))
    return ''

  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

/**
 * 将后端 ISO 时间字符串格式化为 YYYY-MM-DD HH:mm:ss。
 *
 * 支持形如：2025-12-27T02:46:48.000+00:00
 * 容错：null/undefined/空字符串/非法日期 -> 返回空字符串。
 */
export function formatToYMDHMS(input?: string | null): string {
  if (!input)
    return ''

  // 尝试 Date 解析
  const d = new Date(input)
  if (Number.isNaN(d.getTime()))
    return ''

  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const min = String(d.getMinutes()).padStart(2, '0')
  const ss = String(d.getSeconds()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${min}:${ss}`
}
