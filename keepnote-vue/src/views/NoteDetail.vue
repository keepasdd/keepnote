<template>
  <div class="detail-panel">
    <!-- 空状态 -->
    <div v-if="!note && !isEditing" class="empty-detail">
      <div class="empty-icon">
        <el-icon size="24"><Document /></el-icon>
      </div>
      <div class="empty-title">选择一篇笔记</div>
      <div class="empty-sub">从左侧列表中点击查看详情</div>
    </div>

    <!-- 编辑/新建模式 -->
    <template v-else-if="isEditing">
      <div class="detail-toolbar">
        <span class="note-id" style="color:var(--accent)">● {{ editForm.id ? '编辑笔记' : '新建笔记' }}</span>
        <div class="actions">
          <el-button size="small" @click="cancelEdit">取消</el-button>
          <el-button size="small" type="primary" :loading="saving" @click="save">保存</el-button>
        </div>
      </div>
      <div class="detail-meta">
        <input
          v-model="editForm.title"
          class="title-input"
          placeholder="笔记标题…"
          autofocus
        />
        <div class="meta-row">
          <el-select v-model="editForm.categoryId" placeholder="选择分类" size="small" clearable style="width:120px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select
            v-model="editForm.tagIds" placeholder="添加标签" size="small" multiple clearable style="width:180px"
          >
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>
      </div>
      <div class="detail-divider" />
      <div class="detail-body">
        <textarea v-model="editForm.content" class="content-editor" placeholder="开始记录你的想法…" />
      </div>
    </template>

    <!-- 查看模式 -->
    <template v-else>
      <div class="detail-toolbar">
        <span class="note-id">#{{ String(note.id).padStart(4, '0') }}</span>
        <div class="actions">
          <el-tooltip content="收藏">
            <el-button :icon="note.isFavorite ? StarFilled : Star" circle size="small" @click="toggleFavorite" />
          </el-tooltip>
          <el-tooltip content="编辑">
            <el-button :icon="Edit" circle size="small" @click="startEdit" />
          </el-tooltip>
          <el-tooltip content="删除">
            <el-button :icon="Delete" circle size="small" type="danger" plain @click="confirmDelete" />
          </el-tooltip>
        </div>
      </div>

      <div class="detail-meta">
        <div class="detail-title">{{ note.title }}</div>
        <div class="info-row">
          <span class="info-item"><el-icon><Calendar /></el-icon> {{ note.updatedAt }}</span>
          <span class="info-item"><el-icon><Document /></el-icon> {{ wordCount }} 字</span>
          <span class="info-item" v-if="note.categoryName">
            <span class="dot" :style="{ background: note.categoryColor }" /> {{ note.categoryName }}
          </span>
        </div>
        <div class="tag-row">
          <span
            v-for="tag in note.tags" :key="tag.id"
            class="tag-pill"
            :style="{ background: tag.color + '18', color: tag.color }"
          >{{ tag.name }}</span>
        </div>
      </div>

      <div class="detail-divider" />

      <div class="stats-row">
        <div class="stat"><div class="stat-num">{{ wordCount }}</div><div class="stat-label">字数</div></div>
        <div class="stat"><div class="stat-num">{{ readTime }}</div><div class="stat-label">分钟读</div></div>
        <div class="stat"><div class="stat-num">{{ note.tags?.length || 0 }}</div><div class="stat-label">标签</div></div>
      </div>

      <div class="detail-body">
        <div class="detail-content" v-html="renderedContent" />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, watch, reactive } from 'vue'
import { Edit, Delete, Star, StarFilled, Calendar, Document } from '@element-plus/icons-vue'
import { addNote, updateNote, deleteNote } from '../api/note'
import { ElMessage, ElMessageBox } from 'element-plus'

const props = defineProps({
  note: { type: Object, default: null },
  isEditing: { type: Boolean, default: false },
  categories: { type: Array, default: () => [] },
  tags: { type: Array, default: () => [] },
})
const emit = defineEmits(['deleted', 'saved', 'cancel-edit', 'toggle-favorite'])

const saving = ref(false)
const editForm = reactive({ id: null, title: '', content: '', categoryId: null, tagIds: [] })

// 进入编辑时填充表单
watch(() => props.note, (n) => {
  if (n && props.isEditing) {
    editForm.id = n.id
    editForm.title = n.title
    editForm.content = n.content || ''
    editForm.categoryId = n.categoryId || null
    editForm.tagIds = n.tags?.map(t => t.id) || []
  }
})

watch(() => props.isEditing, (val) => {
  if (val && !props.note) {
    // 新建模式重置
    editForm.id = null
    editForm.title = ''
    editForm.content = ''
    editForm.categoryId = null
    editForm.tagIds = []
  }
})

const wordCount = computed(() => {
  const text = props.note?.content?.replace(/<[^>]+>/g, '') || ''
  return text.length
})

const readTime = computed(() => Math.max(1, Math.ceil(wordCount.value / 300)))

const renderedContent = computed(() => {
  // 简单处理：把换行转 <br>，实际可接入 markdown 渲染库
  return props.note?.content?.replace(/\n/g, '<br>') || ''
})

function startEdit() {
  editForm.id = props.note.id
  editForm.title = props.note.title
  editForm.content = props.note.content || ''
  editForm.categoryId = props.note.categoryId || null
  editForm.tagIds = props.note.tags?.map(t => t.id) || []
  emit('saved', { ...props.note, _editMode: true })
}

function cancelEdit() { emit('cancel-edit') }

async function save() {
  if (!editForm.title.trim()) return ElMessage.warning('标题不能为空')
  saving.value = true
  try {
    if (editForm.id) {
      await updateNote(editForm)
    } else {
      await addNote(editForm)
    }
    ElMessage.success('保存成功')
    emit('saved')
  } finally {
    saving.value = false
  }
}

async function confirmDelete() {
  await ElMessageBox.confirm('确定删除这篇笔记吗？', '提示', { type: 'warning' })
  await deleteNote(props.note.id)
  ElMessage.success('已删除')
  emit('deleted')
}

function toggleFavorite() { emit('toggle-favorite', props.note.id) }
</script>

<style scoped>
.detail-panel {
  background: var(--surface);
  display: flex; flex-direction: column;
  height: 100vh; overflow: hidden;
}

.empty-detail {
  flex: 1; display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  color: var(--text-dim); gap: 12px;
}
.empty-icon {
  width: 48px; height: 48px;
  border: 2px dashed var(--border-active); border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: var(--text-muted);
}
.empty-title { font-size: 14px; color: var(--text-muted); }
.empty-sub { font-size: 12px; }

.detail-toolbar {
  padding: 20px 22px 0;
  display: flex; align-items: center; justify-content: space-between;
  flex-shrink: 0;
}
.note-id { font-family: var(--font-mono); font-size: 11px; color: var(--text-dim); }
.actions { display: flex; gap: 6px; }

.detail-meta { padding: 18px 22px 0; flex-shrink: 0; }
.detail-title {
  font-family: var(--font-serif); font-size: 20px; font-weight: 600;
  color: var(--text); line-height: 1.35; margin-bottom: 10px;
}

.title-input {
  width: 100%; background: none; border: none; outline: none;
  font-family: var(--font-serif); font-size: 20px; font-weight: 600;
  color: var(--text); letter-spacing: -0.01em; line-height: 1.35;
  margin-bottom: 14px; display: block;
}

.info-row { display: flex; align-items: center; gap: 14px; margin-bottom: 12px; flex-wrap: wrap; }
.info-item { display: flex; align-items: center; gap: 5px; font-size: 11px; color: var(--text-muted); font-family: var(--font-mono); }
.dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; }

.meta-row { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }

.tag-row { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 14px; }
.tag-pill { font-size: 11px; padding: 3px 10px; border-radius: 6px; }

.detail-divider { height: 1px; background: var(--border); margin: 0 22px; flex-shrink: 0; }

.stats-row {
  display: flex; gap: 1px; padding: 14px 22px 0; flex-shrink: 0;
}
.stat {
  flex: 1; background: var(--surface2); padding: 10px 12px; text-align: center;
}
.stat:first-child { border-radius: 8px 0 0 8px; }
.stat:last-child { border-radius: 0 8px 8px 0; }
.stat-num { font-family: var(--font-mono); font-size: 18px; font-weight: 700; color: var(--accent); line-height: 1; }
.stat-label { font-size: 10px; color: var(--text-muted); margin-top: 3px; }

.detail-body { flex: 1; overflow-y: auto; padding: 20px 22px 24px; }
.detail-content { font-size: 14px; color: var(--text-muted); line-height: 1.9; }

.content-editor {
  width: 100%; height: 100%; min-height: 400px;
  background: none; border: none; outline: none; resize: none;
  font-family: var(--font-body); font-size: 14px;
  color: var(--text-muted); line-height: 1.9;
}

:deep(.el-select-dropdown__item) {
  color: #333 !important;
}
</style>

