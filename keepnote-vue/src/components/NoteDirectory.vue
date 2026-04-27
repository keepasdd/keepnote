<template>
  <div class="note-directory">

    <!-- 头部 -->
    <div class="directory-header">
      <div class="directory-header-left">
        <span class="directory-icon">≡</span>
        <span class="directory-title">笔记目录</span>
      </div>
      <span class="directory-count" v-if="!loading">{{ notes.length }}</span>
    </div>

    <div class="directory-divider" />

    <!-- 加载中 -->
    <div v-if="loading" class="directory-loading">
      <div v-for="i in 5" :key="i" class="skeleton-item">
        <div class="skeleton-dot" />
        <div class="skeleton-line" />
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="notes.length === 0" class="directory-empty">
      <div class="empty-icon">○</div>
      <div class="empty-text">暂无笔记</div>
    </div>

    <!-- 列表 -->
    <div v-else class="note-list">
      <div
          v-for="(note, index) in notes" :key="note.id"
          :class="['note-list-item', { 'is-active': note.id === currentNoteId }]"
          @click="selectNote(note.id)"
      >
        <span class="item-index">{{ String(index + 1).padStart(2, '0') }}</span>
        <span v-if="note.isPinned" class="pin-icon" title="已置顶">📌</span>
        <span class="item-title">{{ note.title }}</span>
        <span class="item-arrow" v-if="note.id === currentNoteId">›</span>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { getNoteList } from '../api/note'
import { ElMessage } from 'element-plus'

const props = defineProps({
  context: {
    type: Object,
    default: () => ({ type: 'all' })
  },
  currentNoteId: {
    type: Number,
    default: null
  }
})

const emit = defineEmits(['note-selected'])

const notes = ref([])
const loading = ref(true)

async function fetchNotes() {
  loading.value = true
  try {
    const params = { page: 1, pageSize: 9999 }
    if (props.context.type === 'category' && props.context.categoryId) {
      params.categoryId = props.context.categoryId
    }
    const res = await getNoteList(params)
    notes.value = res.records || res.rows || []
  } catch (error) {
    ElMessage.error('获取笔记列表失败')
    console.error('Failed to fetch notes:', error)
  } finally {
    loading.value = false
  }
}

function selectNote(noteId) {
  emit('note-selected', noteId)
}

watch(() => props.context, fetchNotes, { immediate: true, deep: true })
</script>

<style scoped>
.note-directory {
  display: flex;
  flex-direction: column;
  height: 100%;
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
}

.directory-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px 14px;
  flex-shrink: 0;
}
.directory-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.directory-icon {
  font-size: 14px;
  color: var(--accent);
  line-height: 1;
}
.directory-title {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-dim);
  letter-spacing: 2px;
  text-transform: uppercase;
}
.directory-count {
  font-size: 10px;
  font-family: var(--font-mono);
  color: var(--text-dim);
  background: var(--surface3);
  padding: 2px 7px;
  border-radius: 10px;
  border: 1px solid var(--border);
}

.directory-divider {
  height: 1px;
  background: var(--border);
  margin-bottom: 12px;
  flex-shrink: 0;
}

.directory-loading {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}
.skeleton-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
}
.skeleton-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--border);
  flex-shrink: 0;
}
.skeleton-line {
  height: 10px;
  width: 60%;
  border-radius: 4px;
  background: var(--surface3);
  animation: shimmer 1.5s infinite;
}
@keyframes shimmer {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.directory-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px 0;
  color: var(--text-dim);
}
.empty-icon { font-size: 24px; opacity: 0.4; }
.empty-text { font-size: 12px; letter-spacing: 0.5px; }

.note-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.note-list::-webkit-scrollbar { width: 3px; }
.note-list::-webkit-scrollbar-thumb { background: var(--border); border-radius: 3px; }

.note-list-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 10px;
  border-radius: 8px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.15s;
  min-width: 0;
}
.note-list-item:hover {
  background: var(--surface3);
  border-color: var(--border);
}
.note-list-item.is-active {
  background: rgba(var(--accent-rgb), 0.12);
  border-color: rgba(var(--accent-rgb), 0.35);
}

.item-index {
  font-family: var(--font-mono);
  font-size: 9px;
  color: var(--text-dim);
  flex-shrink: 0;
  opacity: 0.6;
  line-height: 1;
}
.note-list-item.is-active .item-index {
  color: rgba(var(--accent-rgb), 0.8);
  opacity: 1;
}

.item-title {
  flex: 1;
  font-size: 12.5px;
  color: var(--text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}
.note-list-item.is-active .item-title {
  color: var(--accent);
  font-weight: 500;
}

.item-arrow {
  font-size: 16px;
  color: rgba(var(--accent-rgb), 0.7);
  flex-shrink: 0;
  line-height: 1;
}

.pin-icon {
  font-size: 12px;
  flex-shrink: 0;
  line-height: 1;
}
</style>