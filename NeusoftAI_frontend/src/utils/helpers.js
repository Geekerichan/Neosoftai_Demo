/**
 * 公共工具函数 - 避免各 Vue 组件重复定义
 */

/** 故障类型 → 中文名 */
export function getTypeName(type) {
  const map = { SERVER: '服务器', OFFICE: '办公设备', OTHER: '其他' }
  return map[type] || '其他'
}

/** 格式化时间字符串 (2026-04-20T10:30:00 → 2026-04-20 10:30) */
export function formatTime(timeStr) {
  if (!timeStr) return ''
  return timeStr.substring(0, 16).replace('T', ' ')
}

/** Markdown 轻量渲染 */
export function renderMarkdown(md) {
  if (!md) return ''
  let html = md
  html = html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 代码块
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, (_, lang, code) => `<pre class="md-code"><code class="lang-${lang}">${code.trim()}</code></pre>`)
  // 行内代码
  html = html.replace(/`([^`\n]+)`/g, '<code class="md-inline-code">$1</code>')
  // 标题
  html = html.replace(/### (.+)$/gm, '<h3>$1</h3>')
           .replace(/## (.+)$/gm, '<h2>$1</h2>')
           .replace(/^# (.+)$/gm, '<h1>$1</h1>')
  // 加粗 / 无序列表 / 有序列表 / 引用 / 分割线
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/^[\-\*] (.+)$/gm, '<li>$1</li>')
  html = html.replace(/((<li>.*<\/li>\n?)+)/g, '<ul>$1</ul>')
  html = html.replace(/^\d+\.\s+(.+)$/gm, '<oli>$1</oli>')
  html = html.replace(/((<oli>.*<\/oli>\n?)+)/g, (match) =>
    `<ol>${match.replace(/<\/?oli>/g, (tag) => tag === '<oli>' ? '<li>' : '</li>')}</ol>`
  )
  html = html.replace(/^&gt;\s?(.+)$/gm, '<blockquote>$1</blockquote>')
  html = html.replace(/^(---|\*\*\*)$/gm, '<hr>')

  // 段落包裹
  const lines = html.split('\n')
  const result = []
  let buffer = []
  for (const line of lines) {
    const trimmed = line.trim()
    if (trimmed === '') {
      if (buffer.length > 0) { result.push('<p>' + buffer.join('<br>') + '</p>'); buffer = [] }
    } else if (/^<(h[1-6]|ul|ol|li|blockquote|hr|pre|div)/.test(trimmed)) {
      if (buffer.length > 0) { result.push('<p>' + buffer.join('<br>') + '</p>'); buffer = [] }
      result.push(trimmed)
    } else { buffer.push(trimmed) }
  }
  if (buffer.length > 0) result.push('<p>' + buffer.join('<br>') + '</p>')
  return result.join('\n')
}

/** 文件大小格式化 */
export function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

/** 知识分类 → 中文名 */
export function getCategoryName(c) {
  return { SERVER: '服务器运维', OFFICE: '办公设备', NETWORK: '网络安全', DATA: '数据管理', MAINTAIN: '日常维护', OTHER: '其他' }[c] || c
}
