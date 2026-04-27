<template>
  <div class="note-list-panel">
    <!-- 头部 -->
    <div class="list-header">
      <div>
        <div class="list-title">{{ title }}</div>
        <div class="list-subtitle">共 {{ total }} 篇</div>
      </div>
      <el-button type="primary" class="btn-new" @click="emit('new-note')">
        <el-icon><Plus /></el-icon> 新建笔记
      </el-button>
    </div>

    <!-- 筛选 -->
    <div class="filter-bar">
      <el-radio-group v-model="dateFilter" size="small" @change="onFilterChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="today">今天</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 标签筛选行 -->
    <div class="tags-row" v-if="tags.length">
      <span
        v-for="tag in tags" :key="tag.id"
        class="tag-pill"
        :class="{ active: activeTagId === tag.id }"
        :style="{ color: tag.color, background: tag.color + '18', borderColor: tag.color + '40' }"
        @click="toggleTag(tag.id)"
      >
        ● {{ tag.name }}
      </span>
    </div>

    <!-- 列表 -->
    <div class="list-body" v-loading="loading">
      <div v-if="notes.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无笔记" />
      </div>

      <div
        v-for="note in notes" :key="note.id"
        class="note-card"
        :class="{ active: activeNoteId === note.id }"
        @click="emit('select', note.id)"
      >
        <div class="card-header">
          <div class="card-title">{{ note.title }}</div>
          <div class="card-date">{{ note.updatedAt }}</div>
        </div>
        <div class="card-preview">{{ note.content?.replace(/<[^>]+>/g, '').slice(0, 80) }}…</div>
        <div class="card-footer">
          <span
            v-for="tag in note.tags" :key="tag.id"
            class="note-tag"
            :style="{ background: tag.color + '18', color: tag.color }"
          >{{ tag.name }}</span>
          <span class="note-category" v-if="note.categoryName">
            <span class="dot" :style="{ background: note.categoryColor }" />
            {{ note.categoryName }}
          </span>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="pagination" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        small
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'

const props = defineProps({
  notes: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] },
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 20 },
  loading: { type: Boolean, default: false },
  activeNoteId: { type: Number, default: null },
  title: { type: String, default: '全部笔记' },
})
const emit = defineEmits(['select', 'new-note', 'filter-change'])

const dateFilter = ref('')
const activeTagId = ref(null)
const currentPage = ref(1)

function toggleTag(id) {
  activeTagId.value = activeTagId.value === id ? null : id
  emitFilter()
}

function onFilterChange() {
  currentPage.value = 1
  emitFilter()
}

function onPageChange(page) {
  currentPage.value = page
  emitFilter()
}

function emitFilter() {
  emit('filter-change', {
    page: currentPage.value,
    dateRange: dateFilter.value,
    tagId: activeTagId.value,
  })
}
</script>

<style scoped>
.note-list-panel {
  display: flex; flex-direction: column;
  height: 100vh; overflow: hidden;
  border-right: 1px solid var(--border);
  background: var(--bg);
}

.list-header {
  padding: 28px 24px 0;
  display: flex; align-items: flex-start;
  justify-content: space-between; gap: 12px;
  flex-shrink: 0;
}

.list-title { font-family: var(--font-serif); font-size: 22px; font-weight: 600; color: var(--text); }
.list-subtitle { font-size: 12px; color: var(--text-muted); margin-top: 3px; }

.btn-new {
  background: var(--accent); border-color: var(--accent);
  font-family: var(--font-mono); font-size: 12px; font-weight: 700;
  box-shadow: 0 2px 12px rgba(58,125,63,0.25); flex-shrink: 0;
}
.btn-new:hover { background: var(--accent-hover); border-color: var(--accent-hover); }

.filter-bar { padding: 16px 24px 10px; flex-shrink: 0; }

.tags-row {
  padding: 0 24px 12px; display: flex; gap: 6px;
  flex-wrap: nowrap; overflow-x: auto; flex-shrink: 0;
}
.tags-row::-webkit-scrollbar { height: 0; }

.tag-pill {
  padding: 4px 10px; border-radius: 6px; font-size: 11px;
  white-space: nowrap; cursor: pointer; border: 1px solid transparent;
  transition: all 0.15s;
}
.tag-pill:hover { filter: brightness(0.9); }
.tag-pill.active { filter: brightness(0.85); }

.list-body { flex: 1; overflow-y: auto; padding: 0 16px 16px; }

.empty-state { display: flex; align-items: center; justify-content: center; height: 200px; }

.note-card {
  background: var(--surface); border: 1px solid var(--border);
  border-radius: 10px; padding: 16px; margin-bottom: 8px;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, transform 0.15s;
  position: relative; overflow: hidden;
}
.note-card::before {
  content: ''; position: absolute; top: 0; left: 0;
  width: 3px; height: 100%; background: transparent; transition: background 0.2s;
}
.note-card:hover { border-color: var(--border-active); background: var(--surface2); transform: translateX(2px); }
.note-card.active { border-color: rgba(58,125,63,0.3); background: rgba(58,125,63,0.04); }
.note-card.active::before { background: var(--accent); }

.card-header { display: flex; align-items: flex-start; justify-content: space-between; gap: 8px; margin-bottom: 6px; }
.card-title { font-size: 14px; font-weight: 500; color: var(--text); flex: 1; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.card-date { font-family: var(--font-mono); font-size: 10px; color: var(--text-dim); white-space: nowrap; }
.card-preview { font-size: 12.5px; color: var(--text-muted); line-height: 1.6; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; margin-bottom: 10px; }
.card-footer { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }

.note-tag { font-size: 10px; padding: 2px 8px; border-radius: 4px; }
.note-category { margin-left: auto; font-size: 10px; color: var(--text-dim); display: flex; align-items: center; gap: 4px; }
.dot { width: 6px; height: 6px; border-radius: 50%; display: inline-block; }

.pagination { padding: 12px 16px; border-top: 1px solid var(--border); display: flex; justify-content: center; flex-shrink: 0; }
</style>
