<template>
  <div class="note-view-container">
    <div class="note-directory-panel">
      <NoteDirectory :context="directoryContext" :current-note-id="note?.id" @note-selected="handleNoteSelected" />
    </div>
    <div class="note-view-main">
    <div class="note-view-header">
      <el-button :icon="ArrowLeft" circle size="small" @click="router.back()" />
      <div class="actions" v-if="note">
        <el-tooltip content="附件">
          <el-button :icon="Paperclip" circle size="small" @click="startEdit" class="attachment-button" />
        </el-tooltip>
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

    <div v-if="loading" class="note-view-loading">
      <el-skeleton :rows="6" animated />
    </div>

    <template v-else-if="note && !isEditing">
      <div class="note-view-meta">
        <div class="note-view-title">{{ note.title }}</div>
        <div class="info-row">
          <span class="info-item"><el-icon><Calendar /></el-icon> 创建: {{ note.createTime }} | 更新: {{ note.updateTime }}</span>
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

      <div class="note-view-divider" />

      <div class="stats-row">
        <div class="stat"><div class="stat-num">{{ wordCount }}</div><div class="stat-label">字数</div></div>
        <div class="stat"><div class="stat-num">{{ readTime }}</div><div class="stat-label">分钟读</div></div>
        <div class="stat"><div class="stat-num">{{ note.tags?.length || 0 }}</div><div class="stat-label">标签</div></div>
      </div>

      <div class="note-view-body">
        <div class="note-view-content" v-html="renderedContent" />
      </div>

      <!-- 只读模式附件展示 -->
      <div v-if="attachments.length" class="attachments-section readonly">
        <div class="attachments-title">附件 ({{ attachments.length }})</div>
        <!-- 图片网格 -->
        <div v-if="imageAttachments.length" class="image-grid">
          <a
            v-for="att in imageAttachments" :key="att.id"
            :href="att.url" target="_blank" rel="noopener"
            class="image-thumb-wrap"
            :title="att.fileName"
          >
            <img :src="att.url" :alt="att.fileName" class="image-thumb" />
            <div class="image-thumb-name">{{ att.fileName }}</div>
          </a>
        </div>
        <!-- 非图片列表 -->
        <div v-if="fileAttachments.length" class="attachments-list" :style="imageAttachments.length ? 'margin-top:10px' : ''">
          <div v-for="att in fileAttachments" :key="att.id" class="attachment-item capsule">
            <el-icon class="att-icon"><Document /></el-icon>
            <span class="att-name" :title="att.fileName">{{ att.fileName }}</span>
            <span class="att-size">{{ formatSize(att.fileSize) }}</span>
            <el-button :icon="Download" circle size="small" @click="downloadAttachment(att)" />
          </div>
        </div>
      </div>
    </template>

    <!-- 编辑模式 -->
    <template v-else-if="isEditing">
      <div class="note-view-meta">
        <input v-model="editForm.title" class="title-input" placeholder="笔记标题…" autofocus />
        <div class="meta-row">
          <el-select v-model="editForm.categoryId" placeholder="选择分类" size="small" clearable style="width:120px">
            <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
          <el-select v-model="editForm.tagIds" placeholder="添加标签" size="small" multiple clearable style="width:180px">
            <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </div>
      </div>
      <div class="note-view-divider" />
      <div class="note-view-body">
        <textarea ref="editorRef" v-model="editForm.content" class="content-editor" placeholder="开始记录你的想法…" @paste="handlePaste" />
      </div>

      <!-- 附件区域 -->
      <div class="attachments-section">
        <div class="attachments-header">
          <span class="attachments-title">附件</span>
          <el-upload :show-file-list="false" :before-upload="handleUpload" multiple>
            <el-button size="small" :icon="Paperclip" :loading="uploading">上传文件</el-button>
          </el-upload>
        </div>
        <div v-if="attachments.length" class="attachments-list">
          <!-- 图片网格（编辑模式） -->
          <div v-if="imageAttachments.length" class="image-grid">
            <div v-for="att in imageAttachments" :key="att.id" class="image-thumb-wrap edit">
              <img :src="att.url" :alt="att.fileName" class="image-thumb" />
              <div class="image-thumb-name">{{ att.fileName }}</div>
              <div class="image-thumb-actions">
                <el-tooltip content="插入引用"><el-button :icon="Plus" circle size="small" @click="insertAttachment(att)" /></el-tooltip>
                <el-tooltip content="下载"><el-button :icon="Download" circle size="small" @click="downloadAttachment(att)" /></el-tooltip>
                <el-tooltip content="删除"><el-button :icon="Close" circle size="small" type="danger" plain @click="removeAttachment(att)" /></el-tooltip>
              </div>
            </div>
          </div>
          <!-- 非图片行列表（编辑模式） -->
          <template v-if="fileAttachments.length">
            <div v-for="att in fileAttachments" :key="att.id" class="attachment-item capsule" :style="imageAttachments.length ? 'margin-top:6px' : ''">
              <el-icon class="att-icon"><Document /></el-icon>
              <span class="att-name" :title="att.fileName">{{ att.fileName }}</span>
              <span class="att-size">{{ formatSize(att.fileSize) }}</span>
              <div class="att-actions">
                <el-tooltip content="插入引用到笔记">
                  <el-button :icon="Plus" circle size="small" @click="insertAttachment(att)" />
                </el-tooltip>
                <el-tooltip content="下载">
                  <el-button :icon="Download" circle size="small" @click="downloadAttachment(att)" />
                </el-tooltip>
                <el-tooltip content="删除附件">
                  <el-button :icon="Close" circle size="small" type="danger" plain @click="removeAttachment(att)" />
                </el-tooltip>
              </div>
            </div>
          </template>
        </div>
        <div v-else class="attachments-empty">暂无附件，上传文件可辅助记录</div>
      </div>

      <div class="edit-footer">
        <el-button @click="isEditing = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </div>
    </template>
    </div>
  </div>
</template>

<script setup>
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Edit, Delete, Star, StarFilled, Calendar, Document, Paperclip, Plus, Download, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getNoteDetail, updateNote, deleteNote, uploadAttachment, deleteAttachment } from '../api/note'
import { getCategoryList } from '../api/category'
import { getTagList } from '../api/tag'
import NoteDirectory from '../components/NoteDirectory.vue'
import { ref, computed, onMounted, reactive, watch } from 'vue'


const route = useRoute()
const router = useRouter()

const note = ref(null)
const loading = ref(true)
const isEditing = ref(false)
const saving = ref(false)
const uploading = ref(false)
const categories = ref([])
const tags = ref([])
const attachments = ref([])
const editorRef = ref(null)
const editForm = reactive({ id: null, title: '', content: '', categoryId: null, tagIds: [] })

const directoryContext = computed(() => {
  if (route.query.context === 'category' && route.query.categoryId) {
    return { type: 'category', categoryId: Number(route.query.categoryId) }
  }
  return { type: 'all' }
})

function handleNoteSelected(noteId) {
  const currentQuery = { ...route.query }
  router.replace({
    name: 'NoteView',
    params: { id: noteId },
    query: currentQuery
  })
}

onMounted(async () => {
  const id = Number(route.params.id)
  const [detail, cats, tagList] = await Promise.all([
    getNoteDetail(id),
    getCategoryList(),
    getTagList(),
  ])
  console.log('后端返回的detail:', detail)
  note.value = detail
  categories.value = cats
  tags.value = tagList
  attachments.value = detail.attachments || []
  loading.value = false
})

const wordCount = computed(() => note.value?.content?.replace(/<[^>]+>/g, '').length || 0)
const readTime = computed(() => Math.max(1, Math.ceil(wordCount.value / 300)))
const renderedContent = computed(() => {
  if (!note.value?.content) return ''
  let content = note.value.content

  console.log('原始content:',JSON.stringify(content))

  // ✅ 先解析图片（此时 \n 还是原始换行，正则能正常匹配整行）
  content = content.replace(
    /!\[(.*?)\]\(([\s\S]*?)\)/g,   // [\s\S]*? 允许 URL 跨行
    (_, alt, url) => {
      const cleanUrl = url.replace(/\s+/g, '')  // 去掉 URL 里的所有空白/换行
      return `<img src="${cleanUrl}" alt="${alt}" class="note-image" style="width:60%;opacity:0.9;transition:all 0.3s ease;border-radius:8px;margin:10px 0;cursor:pointer;" />`
    }
  )

  // ✅ 再解析普通链接
  content = content.replace(
    /\[(.*?)\]\(([\s\S]*?)\)/g,
    (_, text, url) => {
      const cleanUrl = url.replace(/\s+/g, '')
      return `<a href="${cleanUrl}" target="_blank" rel="noopener" style="color:var(--accent);text-decoration:underline;">${text}</a>`
    }
  )

  // ✅ 最后才替换换行符
  content = content.replace(/\n/g, '<br>')

  return content
})
const imageAttachments = computed(() => attachments.value.filter(a => isImage(a.fileName)))
const fileAttachments = computed(() => attachments.value.filter(a => !isImage(a.fileName)))

function isImage(fileName) {
  return /\.(png|jpe?g|gif|webp|svg)$/i.test(fileName)
}

function getFileExtensionByMime(mimeType) {
  const typeMap = {
    'image/png': 'png',
    'image/jpeg': 'jpg',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'image/svg+xml': 'svg',
  }
  return typeMap[mimeType] || 'png'
}

function createPastedImageFile(blob) {
  const extension = getFileExtensionByMime(blob.type)
  const stamp = new Date().toISOString().replace(/[-:TZ.]/g, '').slice(0, 14)
  return new File([blob], `pasted-image-${stamp}.${extension}`, { type: blob.type || 'image/png' })
}

function formatSize(bytes) {
  if (!bytes) return ''
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function startEdit() {
  editForm.id = note.value.id
  editForm.title = note.value.title
  editForm.content = note.value.content || ''
  editForm.categoryId = note.value.categoryId || null
  editForm.tagIds = note.value.tags?.map(t => t.id) || []
  isEditing.value = true
}

async function handleUpload(file) {
  uploading.value = true
  try {
    const result = await uploadAttachment(editForm.id, file)
    attachments.value.push(result)
    ElMessage.success(`${file.name} 上传成功`)
  } catch {
    // error already shown by request interceptor
  } finally {
    uploading.value = false
  }
  return false // prevent el-upload default behavior
}

async function handlePaste(e) {
  const items = Array.from(e.clipboardData?.items || [])
  const imageItems = items.filter(item => item.type.startsWith('image/'))
  if (!imageItems.length) return

  e.preventDefault()

  for (const item of imageItems) {
    const blob = item.getAsFile()
    if (!blob) continue

    const file = createPastedImageFile(blob)
    uploading.value = true
    try {
      const result = await uploadAttachment(editForm.id, file)
      attachments.value.push(result)
      const ref = `![${result.fileName || file.name}](${result.url})`
      insertAttachmentReference(ref)
      ElMessage.success('图片已上传并插入笔记')
    } catch {
      // error already shown by request interceptor
    } finally {
      uploading.value = false
    }
  }
}

function insertAttachmentReference(ref) {
  const textarea = editorRef.value
  if (textarea) {
    const start = textarea.selectionStart
    const end = textarea.selectionEnd
    const before = editForm.content.slice(0, start)
    const after = editForm.content.slice(end)
    const text = `${ref}\n`
    editForm.content = before + text + after
    const cursor = start + text.length
    textarea.setSelectionRange(cursor, cursor)
    textarea.focus()
    return
  }
  editForm.content += `\n${ref}\n`
}

function insertAttachment(att) {
  const ref = isImage(att.fileName)
    ? `![${att.fileName}](${att.url})`
    : `[${att.fileName}](${att.url})`
  insertAttachmentReference(ref)
  ElMessage.success('已插入引用')
}

function downloadAttachment(att) {
  const a = document.createElement('a')
  a.href = att.url
  a.download = att.fileName
  a.click()
}

async function removeAttachment(att) {
  await ElMessageBox.confirm(`确定删除附件 "${att.fileName}"？`, '提示', { type: 'warning' })
  await deleteAttachment(att.id)
  attachments.value = attachments.value.filter(a => a.id !== att.id)
  ElMessage.success('附件已删除')
}

async function save() {
  console.log('保存的cotent:',editForm.content)
  if (!editForm.title.trim()) return ElMessage.warning('标题不能为空')
  saving.value = true
  try {
    await updateNote(editForm)
    ElMessage.success('保存成功')
    const detail = await getNoteDetail(editForm.id)
    note.value = detail
    attachments.value = detail.attachments || []
    // 立即更新标签显示，避免后端延迟
    if (editForm.tagIds && tags.value.length) {
      const tagMap = new Map()
      // 构建标签映射（支持树形结构）
      const buildTagMap = (tagList) => {
        for (const tag of tagList) {
          tagMap.set(tag.id, tag)
          if (tag.children) {
            buildTagMap(tag.children)
          }
        }
      }
      buildTagMap(tags.value)
      note.value.tags = editForm.tagIds.map(id => tagMap.get(id)).filter(Boolean)
    }
    isEditing.value = false
  } finally {
    saving.value = false
  }
}

async function confirmDelete() {
  await ElMessageBox.confirm('确定删除这篇笔记吗？', '提示', { type: 'warning' })
  await deleteNote(note.value.id)
  ElMessage.success('已删除')
  router.replace({ name: 'Home', query: { refresh: true } })
}

async function toggleFavorite() {
  await updateNote({ id: note.value.id, isFavorite: note.value.isFavorite ? 0 : 1 })
  const detail = await getNoteDetail(note.value.id)
  note.value = detail
  attachments.value = detail.attachments || []
}
</script>

<style scoped>
/* ===== 页面基础 ===== */
.note-view-container {
  display: flex;
  min-height: 100vh;
  background: var(--surface);
  color: var(--text);
  font-family: 'Inter', 'PingFang SC', system-ui, sans-serif;
}

.note-directory-panel {
  width: 280px; /* 侧边栏宽度 */
  flex-shrink: 0;
  padding: 24px 16px;
  border-right: 1px solid var(--border);
  background: var(--surface2);
  overflow-y: auto;
}

.note-view-main {
  flex-grow: 1;
  max-width: 760px; /* 保持内容区域最大宽度 */
  margin: 0 auto;
  padding: 32px 24px 60px;
  display: flex;
  flex-direction: column;
}

.note-view-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
  flex-shrink: 0;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border);
}
.note-view-id {
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--text-dim);
  flex: 1;
}
.actions { display: flex; gap: 8px; }

.note-view-loading { padding: 40px 0; }

/* ===== 元信息 ===== */
.note-view-meta { flex-shrink: 0; }
.note-view-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-h);
  line-height: 1.2;
  margin-bottom: 14px;
  letter-spacing: -0.5px;
}

.title-input {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  font-size: 28px;
  font-weight: 700;
  color: var(--text-h);
  line-height: 1.2;
  margin-bottom: 14px;
  display: block;
  letter-spacing: -0.5px;
  caret-color: rgba(var(--accent-rgb), 0.9);
}
.title-input::placeholder { color: var(--text-dim); }

.info-row {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}
.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: var(--text-dim);
  font-family: var(--font-mono);
}
.dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; }

.meta-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 18px;
}
.tag-pill {
  font-size: 11px;
  padding: 4px 11px;
  border-radius: 999px;
  border: 1px solid var(--border);
  color: var(--text);
  background: var(--surface3);
}

/* ===== 分割线 ===== */
.note-view-divider {
  height: 1px;
  background: var(--border);
  margin-bottom: 0;
  flex-shrink: 0;
}

/* ===== 统计条 ===== */
.stats-row {
  display: flex;
  gap: 10px;
  padding: 16px 0;
  flex-shrink: 0;
}
.stat {
  flex: 1;
  background: var(--surface2);
  border: 1px solid var(--border);
  padding: 16px 14px;
  text-align: center;
  border-radius: 14px;
}
.stat:first-child { border-radius: 14px 0 0 14px; border-right: none; }
.stat:last-child  { border-radius: 0 14px 14px 0; border-left: none; }
.stat-num {
  font-family: var(--font-mono);
  font-size: 20px;
  font-weight: 700;
  color: var(--accent);
  line-height: 1;
}
.stat-label {
  font-size: 10px;
  color: var(--text-dim);
  margin-top: 4px;
  text-transform: uppercase;
  letter-spacing: 0.2em;
}

/* ===== 内容体 ===== */
.note-view-body { flex: 1; padding: 24px 0 0; }
.note-view-content {
  font-size: 15px;
  color: var(--text);
  line-height: 1.9;
}

.content-editor {
  width: 100%;
  min-height: 420px;
  height: 100%;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  font-size: 15px;
  color: var(--text);
  line-height: 1.9;
  caret-color: rgba(var(--accent-rgb), 0.9);
}
.content-editor::placeholder { color: var(--text-dim); }

/* ===== 图片悬停效果 ===== */
.note-image:hover {
  width: 100%;
  opacity: 1;
  transform: scale(1.02);
}

/* ===== 附件区域 ===== */
.attachments-section {
  margin-top: 26px;
  border: 1px solid var(--border);
  border-radius: 18px;
  padding: 18px 18px 14px;
  flex-shrink: 0;
  background: var(--surface2);
}
.attachments-section.readonly { background: var(--surface3); }
.attachments-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.attachments-title {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-dim);
  text-transform: uppercase;
  letter-spacing: 1.4px;
}
.attachments-list { display: flex; flex-direction: column; gap: 10px; }
.attachment-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 14px;
  background: var(--surface3);
  border: 1px solid var(--border);
  font-size: 13px;
}
.attachment-item.capsule {
  border-radius: 999px;
  padding: 8px 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  transition: all 0.2s ease;
}
.attachment-item.capsule:hover {
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
  transform: translateY(-1px);
}
.att-icon { color: rgba(210,240,160,0.98); flex-shrink: 0; }
.att-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text);
}
.att-size {
  font-size: 11px;
  color: var(--text-dim);
  font-family: var(--font-mono);
  flex-shrink: 0;
}
.att-actions { display: flex; gap: 6px; flex-shrink: 0; }
.attachments-empty {
  font-size: 12px;
  color: var(--text-dim);
  text-align: center;
  padding: 14px 0 0;
}

/* ===== 图片网格 ===== */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 4px;
}
.image-thumb-wrap {
  position: relative;
  width: 100px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
  text-decoration: none;
  cursor: pointer;
  flex-shrink: 0;
  background: var(--surface3);
}
.image-thumb-wrap.edit { width: 110px; }
.image-thumb {
  width: 100%;
  height: 80px;
  object-fit: cover;
  display: block;
}
.image-thumb-name {
  font-size: 10px;
  color: var(--text-dim);
  padding: 4px 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  background: var(--surface3);
}
.image-thumb-actions {
  display: flex;
  justify-content: center;
  gap: 3px;
  padding: 6px;
  background: var(--surface2);
  border-top: 1px solid var(--border);
}

/* ===== 编辑底部操作 ===== */
.edit-footer {
  display: flex; justify-content: flex-end; gap: 8px;
  padding: 16px 0 0; flex-shrink: 0;
}

/* ===== 附件按钮静默设计 ===== */
.attachment-button {
  --el-button-text-color: var(--text-dim);
  --el-button-hover-text-color: var(--text);
}

/* ===== Element Plus 暗色覆盖 ===== */
:deep(.el-button) {
  --el-button-bg-color: var(--surface3);
  --el-button-border-color: var(--border);
  --el-button-text-color: var(--text);
  --el-button-hover-bg-color: var(--surface2);
  --el-button-hover-border-color: var(--border);
  --el-button-hover-text-color: var(--text-h);
}
:deep(.el-button--primary) {
  --el-button-bg-color: rgba(180,210,130,0.20);
  --el-button-border-color: rgba(180,210,130,0.50);
  --el-button-text-color: rgba(210,240,160,0.98);
  --el-button-hover-bg-color: rgba(180,210,130,0.32);
  --el-button-hover-border-color: rgba(180,210,130,0.75);
  --el-button-hover-text-color: var(--text-h);
}
:deep(.el-select__wrapper) {
  background: var(--surface3) !important;
  border: 1px solid var(--border) !important;
  box-shadow: none !important;
  color: var(--text) !important;
}
:deep(.el-select__wrapper.is-focused) {
  border-color: rgba(var(--accent-rgb), 0.60) !important;
  box-shadow: none !important;
}
:deep(.el-select__placeholder) { color: var(--text-dim) !important; }
:deep(.el-skeleton__item) { background: var(--surface2) !important; }
</style>
