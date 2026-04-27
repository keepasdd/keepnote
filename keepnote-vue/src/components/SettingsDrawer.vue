<template>
  <teleport to="body">
    <div v-if="visible" class="settings-backdrop" @click.self="close">
      <div class="settings-drawer" role="dialog" aria-modal="true">
        <aside class="settings-menu">
          <ul>
            <li :class="{ active: tab === 'profile' }" @click="tab = 'profile'">个人资料</li>
            <li :class="{ active: tab === 'ui' }" @click="tab = 'ui'">界面 UI</li>
          </ul>
        </aside>

        <section class="settings-content">
          <header class="settings-header">
            <h3 v-if="tab === 'profile'">个人资料</h3>
            <h3 v-else>界面 UI</h3>
            <button class="close-btn" @click="close">关闭</button>
          </header>

          <div v-if="tab === 'profile'" class="panel">
            <slot name="profile">这里放个人资料表单</slot>
          </div>

          <div v-else class="panel ui-panel">
            <p class="panel-section-title">主题预设</p>
            <div class="presets">
              <button
                v-for="p in presets"
                :key="p.id"
                :class="{ selected: currentAccent === p.accent.toLowerCase() }"
                class="preset"
                @click="selectPreset(p)"
                :title="p.name"
              >
                <span class="swatch" :style="{ background: p.accent }"></span>
                <span class="name">{{ p.name }}</span>
              </button>
            </div>

            <div class="custom">
              <label>自定义颜色</label>
              <input type="color" :value="currentAccent" @input="onColorInput" />
              <button class="reset-btn" @click="resetDefault">恢复默认</button>
            </div>

            <p class="panel-section-title" style="margin-top: 18px;">背景</p>
            <div class="bg-options">
              <button
                class="bg-btn"
                :class="{ selected: currentBgId === 'white' }"
                @click="selectBackground('white')"
                title="白色背景"
              >
                白色
              </button>
              <button
                class="bg-btn"
                :class="{ selected: currentBgId === 'black' }"
                @click="selectBackground('black')"
                title="黑色背景"
              >
                黑色
              </button>
              <button
                class="bg-btn"
                :class="{ selected: currentBgId === 'tint' }"
                @click="selectBackground('tint')"
                title="随强调色自动生成深色背景"
              >
                跟随预设
              </button>
            </div>

            <p class="hint">主题会立即生效并保存在本地，下次打开仍然生效。</p>
          </div>
        </section>
      </div>
    </div>
  </teleport>
</template>

<script setup>
import { ref, watch, onBeforeUnmount } from 'vue'
import { THEME_PRESETS, applyTheme, saveTheme, getStoredTheme } from '../utils/theme'

const props = defineProps({ visible: Boolean })
const emit = defineEmits(['update:visible'])

const visible = ref(!!props.visible)

// sync prop -> local
watch(() => props.visible, v => (visible.value = v))

function setBodyLocked(lock) {
  if (typeof document === 'undefined') return
  document.body.style.overflow = lock ? 'hidden' : ''
}

function close() {
  emit('update:visible', false)
}

const tab = ref('profile')
const presets = THEME_PRESETS

const stored = getStoredTheme()
const currentAccent = ref((stored && stored.accent) ? stored.accent.toLowerCase() : (presets[0].accent.toLowerCase()))
const currentBgId = ref((stored && stored.bgId) ? stored.bgId : 'tint')

watch(visible, (v) => setBodyLocked(v), { immediate: true })
onBeforeUnmount(() => setBodyLocked(false))

function selectPreset(preset) {
  const t = { id: preset.id, accent: preset.accent, bgId: preset.bgId || 'tint' }
  applyTheme(t)
  saveTheme(t)
  currentAccent.value = preset.accent.toLowerCase()
  currentBgId.value = t.bgId
}

function onColorInput(e) {
  const color = e.target.value || '#7eba6c'
  const t = { id: 'custom', accent: color, bgId: 'tint' }
  applyTheme(t)
  saveTheme(t)
  currentAccent.value = color.toLowerCase()
  currentBgId.value = 'tint'
}

function resetDefault() {
  const def = { id: 'moss', accent: presets[0].accent, bgId: presets[0].bgId || 'tint' }
  applyTheme(def)
  saveTheme(def)
  currentAccent.value = def.accent.toLowerCase()
  currentBgId.value = def.bgId
}

function selectBackground(bgId) {
  // 保持当前强调色不变，只切换背景
  const accent = currentAccent.value
  const t = { id: stored.id || 'moss', accent, bgId }
  // tint 模式下背景会依据 accent 自动生成
  applyTheme(t)
  saveTheme(t)
  currentBgId.value = bgId
}
</script>

<style scoped>
.settings-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.28);
  z-index: 9000;
  display: flex;
  justify-content: center;
  align-items: center;
}
.settings-drawer {
  width: 900px;
  max-width: calc(100% - 40px);
  height: 80vh;
  background: var(--bg, rgba(20,26,21,0.98));
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: var(--shadow);
}
.settings-menu {
  width: 200px;
  background: var(--surface2);
  border-right: 1px solid var(--border);
}
.settings-menu ul { list-style: none; margin: 0; padding: 12px; }
.settings-menu li { padding: 12px 10px; cursor: pointer; color: var(--text-muted); border-radius: 6px; }
.settings-menu li.active { background: var(--accent-bg); color: var(--accent); font-weight: 700; }

.settings-content { flex: 1; padding: 18px 22px; display: flex; flex-direction: column; min-height: 0; }
.settings-header { display:flex; justify-content: space-between; align-items:center; margin-bottom: 10px; }
.close-btn { background: none; border: none; cursor: pointer; color: var(--text-dim); }

.panel { overflow: auto; padding: 8px 4px 12px; flex: 1; min-height: 0; }
.panel-section-title { font-weight: 600; color: var(--text-h); margin-bottom: 8px; }

.presets { display:flex; gap:8px; flex-wrap:wrap; margin:8px 0 16px; }
.preset { display:flex; align-items:center; gap:8px; padding:6px 8px; border:1px solid var(--border); background:transparent; cursor:pointer; border-radius:6px; color: var(--text-h); }
.preset.selected { outline: 2px solid var(--accent); }
.swatch { width:20px; height:20px; border-radius:50%; border:1px solid var(--border); display:inline-block; }
.name { font-size: 13px; }

.custom { display:flex; align-items:center; gap:12px; margin-top:8px; }
.reset-btn { background: transparent; border: 1px solid var(--border); padding: 6px 10px; border-radius: 6px; color: var(--text-h); cursor: pointer; }
.hint { margin-top: 10px; color: var(--text-dim); font-size: 12px; }

.bg-options {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin: 10px 0 16px;
}
.bg-btn {
  padding: 10px 14px;
  border-radius: 6px;
  border: 1px solid var(--border);
  background: transparent;
  color: var(--text-h);
  cursor: pointer;
  transition: transform 0.15s, border-color 0.2s, background 0.2s;
}
.bg-btn:hover { transform: translateY(-1px); }
.bg-btn.selected {
  border-color: var(--accent);
  box-shadow: 0 0 0 2px rgba(var(--accent-rgb),0.12);
}
</style>
