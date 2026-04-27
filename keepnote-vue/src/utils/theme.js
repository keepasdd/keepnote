const THEME_STORAGE_KEY = 'keepnote-theme'

export const THEME_PRESETS = [
  // 说明：背景由 applyTheme 根据 accent 计算（tint 模式）。
  // 白色/黑色由额外背景按钮触发（white/black 模式）。
  { id: 'moss', name: '苔绿', accent: '#7eba6c', bgId: 'tint' },
  { id: 'ocean', name: '海蓝', accent: '#3b82f6', bgId: 'tint' },
  { id: 'sunset', name: '日落橙', accent: '#f97316', bgId: 'tint' },
  { id: 'rose', name: '玫瑰红', accent: '#e11d48', bgId: 'tint' },
  { id: 'grape', name: '葡萄紫', accent: '#8b5cf6', bgId: 'tint' },
]

const BG_PRESETS = {
  white: {
    id: 'white',
    bg: '#f6f7f9',
    mode: 'light',
  },
  black: {
    id: 'black',
    bg: '#0b0f0c',
    mode: 'dark',
  },
}

const DEFAULT_THEME = {
  id: 'moss',
  accent: '#7eba6c',
  bgId: 'tint',
}

function normalizeHex(hex) {
  const raw = (hex || '').trim().replace('#', '')
  if (!raw) return null
  if (raw.length === 3) {
    return `#${raw.split('').map(c => c + c).join('')}`
  }
  if (raw.length === 6) return `#${raw}`
  return null
}

function hexToRgb(hex) {
  const normalized = normalizeHex(hex)
  if (!normalized) return { r: 126, g: 186, b: 108 }
  const value = normalized.slice(1)
  return {
    r: parseInt(value.slice(0, 2), 16),
    g: parseInt(value.slice(2, 4), 16),
    b: parseInt(value.slice(4, 6), 16),
  }
}

function clamp(v) {
  return Math.max(0, Math.min(255, Math.round(v)))
}

function rgbToHex({ r, g, b }) {
  return `#${[r, g, b].map(v => clamp(v).toString(16).padStart(2, '0')).join('')}`
}

function mixRgb(base, target, ratio) {
  const k = Math.max(0, Math.min(1, ratio))
  return {
    r: clamp(base.r + (target.r - base.r) * k),
    g: clamp(base.g + (target.g - base.g) * k),
    b: clamp(base.b + (target.b - base.b) * k),
  }
}

function withAlpha(rgb, alpha) {
  return `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${alpha})`
}

function asRgbVar(rgb) {
  return `${rgb.r}, ${rgb.g}, ${rgb.b}`
}

export function getStoredTheme() {
  try {
    const text = localStorage.getItem(THEME_STORAGE_KEY)
    if (!text) return { ...DEFAULT_THEME }
    const parsed = JSON.parse(text)
    if (!parsed || typeof parsed !== 'object') return { ...DEFAULT_THEME }
    // 兼容旧数据：只存了 id，或没有 bgId/accent
    const id = parsed.id ?? DEFAULT_THEME.id
    const preset = THEME_PRESETS.find(item => item.id === id)
    const accent = normalizeHex(parsed.accent) || preset?.accent || DEFAULT_THEME.accent
    const bgId = parsed.bgId ?? preset?.bgId ?? DEFAULT_THEME.bgId
    return { id, accent: accent.toLowerCase(), bgId }
  } catch {
    return { ...DEFAULT_THEME }
  }
}

function resolveTheme(theme) {
  const input = theme || getStoredTheme()
  const preset = THEME_PRESETS.find(item => item.id === input.id)
  const accent = normalizeHex(input.accent) || preset?.accent || DEFAULT_THEME.accent
  const bgId = input.bgId || preset?.bgId || DEFAULT_THEME.bgId
  return { id: input.id || preset?.id || 'custom', accent: accent.toLowerCase(), bgId }
}

export function applyTheme(theme) {
  const resolved = resolveTheme(theme)
  const accentRgb = hexToRgb(resolved.accent)
  const hoverRgb = mixRgb(accentRgb, { r: 255, g: 255, b: 255 }, 0.12)
  const softRgb = mixRgb(accentRgb, { r: 255, g: 255, b: 255 }, 0.35)
  const contrastRgb = mixRgb(accentRgb, { r: 255, g: 255, b: 255 }, 0.55)

  const root = document.documentElement

  // 背景：tint 模式会根据 accent 动态生成深色背景
  if (resolved.bgId === BG_PRESETS.white.id) {
    const bg = BG_PRESETS.white.bg
    root.style.setProperty('--bg', bg)
    // 白色模式：尽量让各组件面层背景为纯白
    root.style.setProperty('--surface', '#ffffff')
    root.style.setProperty('--surface2', '#ffffff')
    root.style.setProperty('--surface3', '#ffffff')
    root.style.setProperty('--text', 'rgba(0,0,0,0.86)')
    root.style.setProperty('--text-h', '#111827')
    root.style.setProperty('--text-muted', 'rgba(0,0,0,0.62)')
    root.style.setProperty('--text-dim', 'rgba(0,0,0,0.45)')
    root.style.setProperty('--border', 'rgba(0,0,0,0.14)')
    root.style.setProperty('--border-active', 'rgba(0,0,0,0.26)')
    root.style.setProperty('--code-bg', 'rgba(0,0,0,0.04)')
    root.style.setProperty('--social-bg', 'rgba(0,0,0,0.05)')
    root.style.colorScheme = 'light'
    root.classList.remove('dark')
  } else if (resolved.bgId === BG_PRESETS.black.id) {
    const bg = BG_PRESETS.black.bg
    root.style.setProperty('--bg', bg)
    // 保持原暗色系变量，让白字仍可读
    root.style.setProperty('--surface', '#273029')
    root.style.setProperty('--surface2', 'rgba(255,255,255,0.07)')
    root.style.setProperty('--surface3', 'rgba(255,255,255,0.11)')
    root.style.setProperty('--text', 'rgba(255,255,255,0.95)')
    root.style.setProperty('--text-h', '#ffffff')
    root.style.setProperty('--text-muted', 'rgba(255,255,255,0.68)')
    root.style.setProperty('--text-dim', 'rgba(255,255,255,0.45)')
    root.style.setProperty('--border', 'rgba(255,255,255,0.14)')
    root.style.setProperty('--border-active', 'rgba(255,255,255,0.28)')
    root.style.setProperty('--code-bg', 'rgba(255,255,255,0.05)')
    root.style.setProperty('--social-bg', 'rgba(255,255,255,0.06)')
    root.style.colorScheme = 'dark'
    root.classList.add('dark')
  } else {
    // tint：把 accent 混到黑色，形成深色背景
    const bgRgb = mixRgb(accentRgb, { r: 0, g: 0, b: 0 }, 0.78)
    const bg = rgbToHex(bgRgb)
    root.style.setProperty('--bg', bg)
    root.style.setProperty('--surface', '#273029')
    root.style.setProperty('--surface2', 'rgba(255,255,255,0.07)')
    root.style.setProperty('--surface3', 'rgba(255,255,255,0.11)')
    root.style.setProperty('--text', 'rgba(255,255,255,0.95)')
    root.style.setProperty('--text-h', '#ffffff')
    root.style.setProperty('--text-muted', 'rgba(255,255,255,0.68)')
    root.style.setProperty('--text-dim', 'rgba(255,255,255,0.45)')
    root.style.setProperty('--border', 'rgba(255,255,255,0.14)')
    root.style.setProperty('--border-active', 'rgba(255,255,255,0.28)')
    root.style.setProperty('--code-bg', 'rgba(255,255,255,0.05)')
    root.style.setProperty('--social-bg', 'rgba(255,255,255,0.06)')
    root.style.colorScheme = 'dark'
    root.classList.add('dark')
  }

  root.style.setProperty('--accent', resolved.accent)
  root.style.setProperty('--accent-hover', rgbToHex(hoverRgb))
  root.style.setProperty('--accent-dim', withAlpha(accentRgb, 0.12))
  root.style.setProperty('--accent-bg', withAlpha(accentRgb, 0.1))
  root.style.setProperty('--accent-border', withAlpha(accentRgb, 0.45))
  root.style.setProperty('--accent-rgb', asRgbVar(accentRgb))
  root.style.setProperty('--accent-soft-rgb', asRgbVar(softRgb))
  root.style.setProperty('--accent-contrast-rgb', asRgbVar(contrastRgb))
  const isDark = resolved.bgId !== BG_PRESETS.white.id

  if (isDark) {
    // 暗色：让 Element Plus 用深色背景、白色文字
    root.style.setProperty('--el-bg-color', '#1e2520')
    root.style.setProperty('--el-bg-color-overlay', '#273029')
    root.style.setProperty('--el-text-color-primary', 'rgba(255,255,255,0.92)')
    root.style.setProperty('--el-text-color-regular', 'rgba(255,255,255,0.75)')
    root.style.setProperty('--el-text-color-secondary', 'rgba(255,255,255,0.55)')
    root.style.setProperty('--el-border-color', 'rgba(255,255,255,0.15)')
    root.style.setProperty('--el-border-color-light', 'rgba(255,255,255,0.10)')
    root.style.setProperty('--el-fill-color-blank', '#1e2520')
    root.style.setProperty('--el-mask-color', 'rgba(0,0,0,0.7)')
  } else {
    // 亮色：还原 Element Plus 默认值
    root.style.setProperty('--el-bg-color', '#ffffff')
    root.style.setProperty('--el-bg-color-overlay', '#ffffff')
    root.style.setProperty('--el-text-color-primary', '#303133')
    root.style.setProperty('--el-text-color-regular', '#606266')
    root.style.setProperty('--el-text-color-secondary', '#909399')
    root.style.setProperty('--el-border-color', '#dcdfe6')
    root.style.setProperty('--el-border-color-light', '#e4e7ed')
    root.style.setProperty('--el-fill-color-blank', '#ffffff')
    root.style.setProperty('--el-mask-color', 'rgba(0,0,0,0.5)')
  }
  return resolved
}

export function saveTheme(theme) {
  const resolved = resolveTheme(theme)
  localStorage.setItem(THEME_STORAGE_KEY, JSON.stringify(resolved))
  return resolved
}

export function initTheme() {
  return applyTheme(getStoredTheme())
}
