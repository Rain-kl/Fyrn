/**
 * 格式化字数，超过 1000 使用 k 作为单位
 * 
 * @param count 字数
 * @returns 格式化后的字符串
 */
export function formatWordCount(count?: number | string | null): string {
  if (count == null || count === '')
    return '0'

  const num = typeof count === 'string' ? Number(count) : count
  if (!Number.isFinite(num))
    return '0'

  if (num < 1000)
    return num.toString()

  const kValue = num / 1000
  // 保留一位小数，如果是整数则去掉小数部分
  const fixed = kValue.toFixed(1)
  const normalized = fixed.replace(/\.0$/, '')
  
  return `${normalized}k`
}
