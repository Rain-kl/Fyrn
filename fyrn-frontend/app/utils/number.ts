/**
 * 格式化字数，最小单位千，最大单位万
 * - 小于 1000：显示原始数字
 * - 1000-9999：显示 x.x千
 * - 10000+：显示 x.x万
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

  // 小于 1000，显示原始数字
  if (num < 1000)
    return num.toString()

  // 10000 及以上，使用万作为单位
  if (num >= 10000) {
    const wanValue = num / 10000
    const fixed = wanValue.toFixed(1)
    const normalized = fixed.replace(/\.0$/, '')
    return `${normalized}万`
  }

  // 1000-9999，使用千作为单位
  const qianValue = num / 1000
  const fixed = qianValue.toFixed(1)
  const normalized = fixed.replace(/\.0$/, '')
  
  return `${normalized}千`
}
